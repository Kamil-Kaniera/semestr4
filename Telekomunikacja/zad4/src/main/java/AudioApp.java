import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class AudioApp {
    private static int samplingRate = 44100; // Początkowa częstotliwość próbkowania
    private static int quantizationLevels = 16; // Początkowa liczba poziomów kwantyzacji
    private static TargetDataLine audioLine;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

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
                    receiveAudio(receivePort);
                    break;
                case 2:
                    System.out.print("Podaj adres IP odbiorcy: ");
                    String receiverIp = scanner.next();
                    System.out.print("Podaj port odbiorcy: ");
                    int sendPort = scanner.nextInt();
                    sendAudio(receiverIp, sendPort);
                    break;
                case 3:
                    showAuthors();
                    break;
                case 4:
                    exit = true;
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

                    AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format); // tworzymy reprezentację formatu odbieranego sygnału
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info); // ustawiamy linię dźwiękową z odpowiednimi parametrami
                    line.open(format);  // otwieramy linię dźwiękową z określonym formatem audio
                    line.start(); // rozpoczynamy odtwarzanie danych audio
                    System.out.println("Odbieranie audio...");

                    byte[] buffer = new byte[1024];
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet); // odbieramy przysłane do nas pakiety i przesyłamy je do linii dźwiękowej aby następnie je odtworzyć
                        line.write(packet.getData(), 0, packet.getLength());
                    }
                }
                catch (IOException | LineUnavailableException e) {
                    throw new RuntimeException(e);
                }
        }).start();
    }

    private static void sendAudio(String receiverIp, int port) {
        new Thread(() -> {
            // GUI
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
                    updateAudioSettings();
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
                        updateAudioSettings();
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

                    updateAudioSettings();

                    byte[] buffer = new byte[1024];
                    while (true) {
                        int bytesRead = audioLine.read(buffer, 0, buffer.length);
                        DatagramPacket packet = new DatagramPacket(buffer, bytesRead, address, port);
                        socket.send(packet);
                    }
                }
                catch (LineUnavailableException | IOException e) {
                    e.printStackTrace();
                }
        }).start();
    }

    private static void updateAudioSettings() throws LineUnavailableException {
        if (audioLine != null) {
            audioLine.stop();
            audioLine.close();
        }

        AudioFormat format = new AudioFormat(samplingRate, quantizationLevels, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        audioLine = (TargetDataLine) AudioSystem.getLine(info);
        audioLine.open(format);
        audioLine.start();
    }

    private static void showAuthors() {
        System.out.println("""
                Autorzy programu:
                Kamil Kaniera 247689
                Krzysztof Purgat 247771
                """);
    }
}
