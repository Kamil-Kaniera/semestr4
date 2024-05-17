package view;

import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class UtilityFunctions {

    static String LoadFile(TextArea area, Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        File selectedFile = fileChooser.showOpenDialog(stage);

        ReadAndShowFile(area, selectedFile);

        return selectedFile.getPath();
    }

    static void ReadAndShowFile(TextArea area, File selectedFile) {
        if (selectedFile != null) {
            try {
                FileReader fileReader = new FileReader(selectedFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                String line;
                String text = "";

                while ((line = bufferedReader.readLine()) != null) {
                    text += line;
                }

                area.setText(text);

                bufferedReader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Nie wybrano żadnego pliku.");
        }
    }

    static String getFileExtension(String filePath) {
        // Pobranie indeksu ostatniego wystąpienia kropki (.)
        int dotIndex = filePath.lastIndexOf(".");

        // Sprawdzenie, czy plik ma rozszerzenie
        if (dotIndex != -1 && dotIndex < filePath.length() - 1) {
            // Jeśli tak, wyodrębnij rozszerzenie pliku
            String fileExtension = filePath.substring(dotIndex + 1);
            return fileExtension;
        } else {
            return "";
        }
    }

    static String byteArrayToHexString(byte[] array) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : array) {
            // Konwersja każdego bajtu na jego reprezentację szesnastkową i dodanie do ciągu znaków
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    static byte[] hexStringToByteArray(String hexString) {
        // Usunięcie spacji i znaków nowej linii
        hexString = hexString.replaceAll("\\s+", "");

        // Sprawdzenie, czy ciąg znaków ma parzystą długość
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Nieprawidłowa długość ciągu szesnastkowego.");
        }

        // Obliczenie długości tablicy bajtów
        int length = hexString.length() / 2;

        // Inicjalizacja tablicy bajtów
        byte[] byteArray = new byte[length];

        // Konwersja ciągu znaków szesnastkowych na tablicę bajtów
        for (int i = 0; i < length; i++) {
            int index = i * 2;
            String byteString = hexString.substring(index, index + 2);
            byteArray[i] = (byte) Integer.parseInt(byteString, 16);
        }

        return byteArray;
    }
}
