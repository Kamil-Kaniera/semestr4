package source;
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
        System.out.println("Triple Encrypted: " + new String(encrypted, StandardCharsets.ISO_8859_1).replace("\0", ""));

        // Triple Decrypt
        byte[] decrypted = tripleDes.tripleDeszyfruj(encrypted, key1, key2, key3);
        System.out.println("Triple Decrypted: " + new String(decrypted, StandardCharsets.ISO_8859_1).replace("\0", ""));
    }
}
