package source;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Program {
    public static void main(String[] args) throws IOException {
        // Ustalanie wartości kluczy
//        BigInteger p = new BigInteger("2f97d5337311f0f636d6a8b42469803bc727ad37bd42b49d2f85649e5292960c119541e458cf86da911db89aaf8686bf5b1e9b816f93a016680954f5e017790c5", 16);
//        BigInteger g = new BigInteger("3a3880c02e19b9a192593f822895d60f41b0c201bc93240d27a5ec2950c9a56886e08904e459a6ae3a7da6bcf4ba800d4ff89bfe0e241ddaac81a07ba49968a9", 16);
//        BigInteger h = new BigInteger("1f199a89eabf7c855f680e735aac9b0caaefd95005a3241c2b508b6db93716d067968e7e2465690bc3f54d520c05589899e282d9ec6f11be06818da65b6c3a487", 16);
//        BigInteger a = new BigInteger("5c56902faaae6e686d540907bc0a03d0256d1b9528a111b6b0ea64cd235ed6524aebb70b436cb8ece59acad6a1da66b42b651b18d8f64159fd7d3923fc6121ae", 16);

        BigInteger p = new BigInteger("101");
        BigInteger g = new BigInteger("3");
        BigInteger h = new BigInteger("9");
        BigInteger a = new BigInteger("2");


        // Tworzenie instancji ElGamal z podanymi wartościami
        ElGamal elGamal = new ElGamal(p, g, h, a);

        BigInteger m = BigInteger.valueOf(120);


        BigInteger[] encrypted = elGamal.encrypt(m);

        System.out.println("Zaszyfrowana wiadomość: ");
        System.out.println("c1: " + encrypted[0] + ", c2: " + encrypted[1]);

        BigInteger decrypted = elGamal.decrypt(encrypted);

        System.out.println("Odszyfrowana wiadomość: " + decrypted);

        // Wyświetlenie kluczy
        System.out.println("Public Key (p, g, h): (" + elGamal.getP() + ", " + elGamal.getG() + ", " + elGamal.getH() + ")");
        System.out.println("Private Key (a): " + elGamal.getA());


        // Tworzenie instancji ElGamal
        ElGamal el = new ElGamal(2048);
        // Przygotowanie wiadomości do szyfrowania
        String  messageString = "JESTEM TEST TEST TEST";
        byte[] message = messageString.getBytes(StandardCharsets.ISO_8859_1);

        // Szyfrowanie wiadomości
        byte[] encryptedMessage = el.encryptBytes(message);
        System.out.println("Zaszyfrowana wiadomość: " + Arrays.toString(encryptedMessage));

        // Deszyfrowanie wiadomości
        byte[] decryptedMessage = el.decryptBytes(encryptedMessage);
        String decryptedString = new String(decryptedMessage, StandardCharsets.ISO_8859_1);

        System.out.println("Odszyfrowana wiadomość: " +   decryptedString);
    }
}
