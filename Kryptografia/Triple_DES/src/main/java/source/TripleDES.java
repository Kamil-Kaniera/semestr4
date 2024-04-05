package source;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
        byte[] firstEncryption = des1.szyfruj(wiadomosc, klucz1, true);
        byte[] secondDecryption = des2.szyfruj(firstEncryption, klucz2, false);

        return des3.szyfruj(secondDecryption, klucz3, true);
    }

    public byte[] tripleDeszyfruj(byte[] wiadomosc, byte[] klucz1, byte[] klucz2, byte[] klucz3) {
        byte[] firstDecryption = des3.szyfruj(wiadomosc, klucz3, false);
        byte[] secondEncryption = des2.szyfruj(firstDecryption, klucz2, true);

        return des1.szyfruj(secondEncryption, klucz1, false);
    }

    public String tripleSzyfruj(String wiadomosc, byte[] klucz1, byte[] klucz2, byte[] klucz3) {
        byte[] bity = wiadomosc.getBytes(StandardCharsets.ISO_8859_1);
        byte[] encryptedBytes = tripleSzyfruj(bity, klucz1, klucz2, klucz3);

        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String tripleDeszyfruj(String wiadomosc, byte[] klucz1, byte[] klucz2, byte[] klucz3) {
        byte[] bity = Base64.getDecoder().decode(wiadomosc);
        byte[] decryptedBytes = tripleDeszyfruj(bity, klucz1, klucz2, klucz3);

        return new String(decryptedBytes, StandardCharsets.ISO_8859_1);
    }
}
