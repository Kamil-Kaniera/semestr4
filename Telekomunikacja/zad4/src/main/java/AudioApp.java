import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class AudioApp {
    private static final String RECEIVER_IP = "192.168.43.77";
    private static final int PORT = 12345;
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
                    receiveAudio();
                    break;
                case 2:
                    sendAudio();
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

    private static void receiveAudio() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(PORT);

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
            } catch (IOException | LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private static void sendAudio() {
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
            // GUI

            samplingSlider.addChangeListener(new ChangeListener() {  // jeśli zmienimy częstotliwość próbkowania za pomocą zsuwaka, wywołana zostanie ta funkcja
                public void stateChanged(ChangeEvent event) {
                    samplingRate = ((JSlider) event.getSource()).getValue();  // pobieramy wartość nowo ustawioną wartość próbkowania
                    samplingLabel.setText("Częstotliwość próbkowania: " + samplingRate);  // ustawiamy nowo ustawioną wartość próbkowania w GUI
                    try {
                        updateAudioSettings();                                            // aktualizujemy wysyłany sygnał o nową częstotliwość próbkowania
                    } catch (LineUnavailableException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            quantizationSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    // Sprawdzenie, czy wartość jest wielokrotnością 8
                    int value = ((JSlider) event.getSource()).getValue();              // wszystko tak samo jak w funkcji wyżej tylko zezwalamy na ustawienie poziomu
                    if (value % 8 == 0) {                                              // kwantyzacji na 8 bitów lub 16 bitów, inna wartość jest niedozwolona przez naszą kartę graficzną
                        quantizationLevels = value;
                        quantizationLabel.setText("Liczba poziomów kwantyzacji: " + quantizationLevels);
                        try {
                            updateAudioSettings();
                        } catch (LineUnavailableException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });

            panel.add(samplingLabel);
            panel.add(samplingSlider);
            panel.add(quantizationLabel);
            panel.add(quantizationSlider);

            frame.getContentPane().add(panel);      // dodajemy kolejne elementy do GUI i ustawiamy je na widoczne
            frame.setVisible(true);

            try {
                DatagramSocket socket = new DatagramSocket();                    // tworzymy obiekt gniazda sieciowego
                InetAddress address = InetAddress.getByName(RECEIVER_IP);        // zapisujemy adres hosta, do którego będziemy wysyłać sygnał cyfrowy

                updateAudioSettings();                                           // inicjalizujemy wartości jak próbkowanie i poziom kwantyzacji

                byte[] buffer = new byte[1024];       // tworzymy bufor do przechowywania próbek dźwiękowych odczytanych z linii dźwiękowej
                while (true) {
                    int bytesRead = audioLine.read(buffer, 0, buffer.length);  // zapisujemy odczytane dane audio z linii dźwiękowej w zmiennej bytesRead
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, address, PORT); // tworzymy pakiety z zapisanych bajtów w bytesRead, aby je wysłać na podane gniazdo sieciowe
                    socket.send(packet); // wysyłamy na podane gniazdo sieciowe pakiety
                }
            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void updateAudioSettings() throws LineUnavailableException {
        if (audioLine != null) {    // jeżeli linia dźwiękowa jest otwarta to trzeba ją najpierw zatrzymać, żeby móc zaktualizować właściwości przesyłanego sygnału
            audioLine.stop();
            audioLine.close();
        }

        AudioFormat format = new AudioFormat(samplingRate, quantizationLevels, 1, true, false); // definiujemy format danych audio
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // określamy typ linii dźwiękowej, jaki będziemy chcieli stworzyć
        audioLine = (TargetDataLine) AudioSystem.getLine(info);  // ustawiamy nowe parametry linii dźwiękowej
        audioLine.open(format); // otwieramy linię dźwiękową
        audioLine.start();
    }

    private static void showAuthors() {
        System.out.println("Autorzy programu:\n" +
                "Kamil Kaniera 247689\n" +
                "Krzysztof Purgat 247771\n");
    }
}
