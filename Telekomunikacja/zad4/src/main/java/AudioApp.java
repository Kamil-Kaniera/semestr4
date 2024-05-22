import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

public class AudioApp {
    private static int samplingRate = 44100; // Początkowa częstotliwość próbkowania
    private static int quantizationLevels = 16; // Początkowa liczba poziomów kwantyzacji
    private static TargetDataLine audioLine;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        // Pętla głównego menu
        while (!exit) {
            System.out.println("Menu:");
            System.out.println("1. Odbieraj audio");
            System.out.println("2. Wysyłaj audio");
            System.out.println("3. Wyświetl autorów");
            System.out.println("4. Wyjdź");
            System.out.print("Wybierz opcję: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Podaj port do odbioru: ");
                    int receivePort = scanner.nextInt();
                    receiveAudio(receivePort); // Uruchom funkcję odbierania audio z podanym portem
                    break;
                case 2:
                    System.out.print("Podaj adres IP odbiorcy: ");
                    String receiverIp = scanner.next();
                    System.out.print("Podaj port odbiorcy: ");
                    int sendPort = scanner.nextInt();
                    sendAudio(receiverIp, sendPort); // Uruchom funkcję wysyłania audio z podanym adresem IP i portem
                    break;
                case 3:
                    showAuthors(); // Wyświetl informacje o autorach
                    break;
                case 4:
                    exit = true; // Zakończ program
                    break;
                default:
                    System.out.println("Nieprawidłowy wybór, spróbuj ponownie.");
            }
        }

        scanner.close();
    }

    private static void receiveAudio(int port) {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                // Ustawienie formatu audio do odbioru
                AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                System.out.println("Odbieranie audio...");

                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // Odbierz pakiet audio
                    line.write(packet.getData(), 0, packet.getLength()); // Przesyłaj dane do linii audio
                    double snr = calculateSNR(packet.getData());
                    System.out.println("SNR: " + snr + " dB");
                }
            } catch (IOException | LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void sendAudio(String receiverIp, int port) {
        new Thread(() -> {
            // GUI do ustawienia częstotliwości próbkowania i poziomów kwantyzacji
            JFrame frame = new JFrame("Audio Sender");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JLabel samplingLabel = new JLabel("Częstotliwość próbkowania: " + samplingRate);
            JLabel quantizationLabel = new JLabel("Liczba poziomów kwantyzacji: " + quantizationLevels);

            JSlider samplingSlider = new JSlider(JSlider.HORIZONTAL, 8000, 44100, samplingRate);
            JSlider quantizationSlider = new JSlider(JSlider.HORIZONTAL, 8, 16, quantizationLevels);

            // Ustawienie kroku suwaka na 8, aby tylko wielokrotności 8 były możliwe do wybrania
            quantizationSlider.setMajorTickSpacing(8);
            quantizationSlider.setMinorTickSpacing(8);
            quantizationSlider.setPaintTicks(true);

            samplingSlider.addChangeListener(event -> {
                samplingRate = ((JSlider) event.getSource()).getValue();
                samplingLabel.setText("Częstotliwość próbkowania: " + samplingRate);
                try {
                    updateAudioSettings(); // Aktualizacja ustawień audio po zmianie częstotliwości próbkowania
                } catch (LineUnavailableException e) {
                    throw new RuntimeException(e);
                }
            });

            quantizationSlider.addChangeListener(event -> {
                int value = ((JSlider) event.getSource()).getValue();
                if (value % 8 == 0) {
                    quantizationLevels = value;
                    quantizationLabel.setText("Liczba poziomów kwantyzacji: " + quantizationLevels);
                    try {
                        updateAudioSettings(); // Aktualizacja ustawień audio po zmianie poziomów kwantyzacji
                    } catch (LineUnavailableException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            panel.add(samplingLabel);
            panel.add(samplingSlider);
            panel.add(quantizationLabel);
            panel.add(quantizationSlider);

            frame.getContentPane().add(panel);
            frame.setVisible(true);


            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress address = InetAddress.getByName(receiverIp);

                updateAudioSettings(); // Inicjalizacja ustawień audio

                byte[] buffer = new byte[1024];
                while (true) {
                    int bytesRead = audioLine.read(buffer, 0, buffer.length);
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, address, port);
                    try {
                        socket.send(packet); // Wysłanie pakietu audio
                    } catch (SocketException e) {
                        System.err.println("SocketException: " + e.getMessage());
                    }
                }
            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void updateAudioSettings() throws LineUnavailableException {
        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
        }

        // Ustawienie nowego formatu audio
        AudioFormat format = new AudioFormat(samplingRate, quantizationLevels, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        audioLine = (TargetDataLine) AudioSystem.getLine(info);
        audioLine.open(format);
        audioLine.start();
    }

    private static double calculateSNR(byte[] audioData) {
        double signalPower = 0;
        double noisePower = 0;

        for (int i = 0; i < audioData.length; i += 2) {
            int sample = ((audioData[i + 1] << 8) | (audioData[i] & 0xff));
            signalPower += sample * sample;
        }

        // Zakładamy, że szum to różnica między sygnałem a jego uśrednioną wartością
        double meanSignal = signalPower / (audioData.length / 2);
        for (int i = 0; i < audioData.length; i += 2) {
            int sample = ((audioData[i + 1] << 8) | (audioData[i] & 0xff));
            noisePower += (sample - meanSignal) * (sample - meanSignal);
        }

        signalPower = signalPower / (audioData.length / 2);
        noisePower = noisePower / (audioData.length / 2);

        return 10 * Math.log10(signalPower / noisePower);
    }

    private static void showAuthors() {
        System.out.println("""
                Autorzy programu:
                Kamil Kaniera 247689
                Krzysztof Purgat 247771
                """);
    }
}
