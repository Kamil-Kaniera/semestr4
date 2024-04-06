package source;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Program {
    public static void main(String[] args) {
        byte[] klucz = {26, 4, 0, 56, 17, 0, 0, 3};

        DES obiekt = new DES();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Podaj tekst do zaszyfrowania: ");
        String message = scanner.nextLine();
        scanner.close();

        String zaszyfrowane = obiekt.szyfruj(message, klucz, true);
        System.out.println("Zaszyfrowane: " + zaszyfrowane);

        String odszyfrowane = obiekt.szyfruj(zaszyfrowane, klucz, false);
        System.out.println("Odszyfrowane: " + odszyfrowane);


        TripleDES tripleDes = new TripleDES();
        byte[] key1 = "12345678".getBytes(); // 8-byte key for DES1
        byte[] key2 = "abcdefgh".getBytes(); // 8-byte key for DES2
        byte[] key3 = "ijklmnop".getBytes(); // 8-byte key for DES3
        byte[] message3 = "Hello, Triple DES!".getBytes(StandardCharsets.ISO_8859_1);

        // Triple Encrypt
        byte[] encrypted = tripleDes.tripleSzyfruj(message3, key1, key2, key3);
        System.out.println("Triple Encrypted: " + new String(encrypted, StandardCharsets.ISO_8859_1));

        // Triple Decrypt
        byte[] decrypted = tripleDes.tripleDeszyfruj(encrypted, key1, key2, key3);
        System.out.println("Triple Decrypted: " + new String(decrypted, StandardCharsets.ISO_8859_1).replace("\0", ""));


//        // Pliki DES
//        DES des = new DES();
//        byte[] key = "12345678".getBytes(StandardCharsets.ISO_8859_1);  // Replace with your DES key
//        String inputFilePath = Program.class.getClassLoader().getResource("input.txt").getPath();
//
//        try {
//            // Encrypt file
//            des.szyfrujPlik(inputFilePath, "encrypted.txt", key, true);
//
//            // Decrypt file
//            des.szyfrujPlik("encrypted.txt", "decrypted.txt", key, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String inputPhotoFilePath = Program.class.getClassLoader().getResource("input_photo.webp").getPath();
//
//        try {
//            // Encrypt photo file
//            des.szyfrujPlik(inputPhotoFilePath, "encrypted_photo.webp", key, true);
//
//            // Decrypt photo file
//            des.szyfrujPlik("encrypted_photo.webp", "decrypted_photo.webp", key, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //Pliki TrippleDES
        TripleDES tripleDesFile = new TripleDES();
        byte[] key1_3D = "12345678".getBytes(StandardCharsets.ISO_8859_1);  // Replace with your Triple DES key part 1
        byte[] key2_3D = "87654321".getBytes(StandardCharsets.ISO_8859_1);  // Replace with your Triple DES key part 2
        byte[] key3_3D = "23456789".getBytes(StandardCharsets.ISO_8859_1);  // Replace with your Triple DES key part 3

        String inputFilePath_3D = Program.class.getClassLoader().getResource("input.txt").getPath();
        String inputPhotoFilePath_3D = Program.class.getClassLoader().getResource("input_photo.webp").getPath();

        try {
            // Encrypt file
            tripleDesFile.tripleSzyfrujPlik(inputFilePath_3D, "encrypted_3D.txt", key1_3D, key2_3D, key3_3D, true);

            // Decrypt file
            tripleDesFile.tripleSzyfrujPlik("encrypted_3D.txt", "decrypted_3D.txt", key1_3D, key2_3D, key3_3D, false);

            // Encrypt photo file
            tripleDesFile.tripleSzyfrujPlik(inputPhotoFilePath_3D, "encrypted_photo_3D.webp", key1_3D, key2_3D, key3_3D, true);

            // Decrypt photo file
            tripleDesFile.tripleSzyfrujPlik("encrypted_photo_3D.webp", "decrypted_photo_3D.webp", key1_3D, key2_3D, key3_3D, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
