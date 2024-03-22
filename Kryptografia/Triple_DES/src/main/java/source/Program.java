package source;
import java.io.*;

public class Program {
    public static void main(String[] args) {
        byte[] klucz = {
                0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0,
                1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0,
                0, 1, 1, 0,1, 1, 1, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0,
                1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 1 };

        DES obiekt = new DES();

        Console cnsl
                = System.console();

        String message = cnsl.readLine("Podaj tekst do zaszyfrowania: ");

        String zaszyfrowane = obiekt.szyfruj(message, klucz, true);
        System.out.println("Zaszyfrowane: " + zaszyfrowane);

        String odszyfrowane = obiekt.szyfruj(zaszyfrowane, klucz, false).replace("\0", "");
        System.out.println("Odszyfrowane: " + odszyfrowane);
    }
}
