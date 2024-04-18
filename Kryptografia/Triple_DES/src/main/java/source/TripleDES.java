package source;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TripleDES {

    private final DES des1;
    private final DES des2;
    private final DES des3;

    public TripleDES() {
        this.des1 = new DES();
        this.des2 = new DES();
        this.des3 = new DES();
    }

    public byte[] tripleSzyfruj(byte[] wiadomosc, byte[] klucz1, byte[] klucz2, byte[] klucz3) {
        wiadomosc= dodajPadding(wiadomosc);
        byte[] firstEncryption = des1.szyfruj(wiadomosc, klucz1, true);
        byte[] secondDecryption = des2.szyfruj(firstEncryption, klucz2, false);

        return des3.szyfruj(secondDecryption, klucz3, true);
    }

    public byte[] tripleDeszyfruj(byte[] wiadomosc, byte[] klucz1, byte[] klucz2, byte[] klucz3) {
        byte[] firstDecryption = des3.szyfruj(wiadomosc, klucz3, false);
        byte[] secondEncryption = des2.szyfruj(firstDecryption, klucz2, true);

        return usunPadding(des1.szyfruj(secondEncryption, klucz1, false));

    }

    public void tripleSzyfrujPlik(String inputFile, String outputFile, byte[] key1, byte[] key2, byte[] key3, boolean encrypt) throws IOException {
        Path inputPath = Paths.get(inputFile);
        Path outputPath = Paths.get(outputFile);

        byte[] fileContent = Files.readAllBytes(inputPath);
        byte[] processedContent = new byte[0];

        if (encrypt) {
//            fileContent = dodajPadding(fileContent);
            processedContent = tripleSzyfruj(fileContent, key1, key2, key3);

        } else {
            processedContent = tripleDeszyfruj(fileContent, key1, key2, key3);
//            processedContent = usunPadding(processedContent);
        }

        Files.write(outputPath, processedContent);
    }


    private byte[] dodajPadding(byte[] dane) {
        int brakujaceBajty = 8 - (dane.length % 8);
        byte[] daneZPaddingiem = new byte[dane.length + brakujaceBajty];
        System.arraycopy(dane, 0, daneZPaddingiem, 0, dane.length);
        for (int i = dane.length; i < daneZPaddingiem.length; i++) {
            daneZPaddingiem[i] = (byte) brakujaceBajty;
        }
        return daneZPaddingiem;
    }

    private byte[] usunPadding(byte[] dane) {
        int iloscPaddingu = dane[dane.length - 1];
        return Arrays.copyOfRange(dane, 0, dane.length - iloscPaddingu);
    }

}
