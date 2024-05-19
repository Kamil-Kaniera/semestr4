package source;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ElGamal {
    private BigInteger p, g, h, a;
    private SecureRandom random;

    public ElGamal(int bitLength) {
        random = new SecureRandom();
        do {
            p = BigInteger.probablePrime(bitLength, random);
        } while (!millerRabinTest(p, 10)); // 10 iterations for Miller-Rabin test
        g = new BigInteger(bitLength - 1, random);
        a = new BigInteger(bitLength - 1, random);
        h = g.modPow(a, p);
    }

    public ElGamal(BigInteger p, BigInteger g, BigInteger h, BigInteger a) {
        this.p = p;
        this.g = g;
        this.h = h;
        this.a = a;
        random = new SecureRandom();

    }

    public ElGamal(byte[] pBytes, byte[] gBytes, byte[] hBytes, byte[] aBytes) {
        this.p = new BigInteger(1, pBytes);
        this.g = new BigInteger(1, gBytes);
        this.h = new BigInteger(1, hBytes);
        this.a = new BigInteger(1, aBytes);
        random = new SecureRandom();
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getH() {
        return h;
    }

    public BigInteger getA() {
        return a;
    }

    public BigInteger[] encrypt(BigInteger message) {
        BigInteger r = new BigInteger(p.bitLength() - 1, random);
        BigInteger c1 = g.modPow(r, p);
        BigInteger c2 = message.multiply(h.modPow(r, p)).mod(p);
        return new BigInteger[]{c1, c2};
    }

    public BigInteger decrypt(BigInteger[] ciphertext) {
        BigInteger c1 = ciphertext[0];
        BigInteger c2 = ciphertext[1];
        BigInteger s = c1.modPow(a, p);
        BigInteger sInverse = s.modInverse(p);
        return c2.multiply(sInverse).mod(p);
    }

    private int getMaxBlockSize() {
        return (p.bitLength() + 7) / 8 + 1; // Max block size in bytes
    }

    public byte[] encryptBytes(byte[] message) {
        int blockSize = (p.bitLength() - 1) / 8; // Number of bytes required to represent a block in ElGamal
        int maxBlockSize = getMaxBlockSize(); // Max size of c1 and c2
        List<byte[]> encryptedBlocks = new ArrayList<>();

        // Pad the message before splitting into blocks
        message = dodajPadding(message, blockSize);

        List<byte[]> blocks = splitIntoBlocks(message, blockSize);

        for (byte[] block : blocks) {
            BigInteger messageBlock = new BigInteger(1, block);
            BigInteger[] encryptedBlock = encrypt(messageBlock);

            byte[] c1Bytes = encryptedBlock[0].toByteArray();
            byte[] c2Bytes = encryptedBlock[1].toByteArray();

            // Add padding length bytes (1 byte each)
            byte c1PaddingByte = (byte) (maxBlockSize - c1Bytes.length);
            byte c2PaddingByte = (byte) (maxBlockSize - c2Bytes.length);

            // Pad c1 and c2 to maxBlockSize
            c1Bytes = padToLength(c1Bytes, maxBlockSize);
            c2Bytes = padToLength(c2Bytes, maxBlockSize);

            encryptedBlocks.add(c1Bytes);
            encryptedBlocks.add(new byte[]{c1PaddingByte});
            encryptedBlocks.add(c2Bytes);
            encryptedBlocks.add(new byte[]{c2PaddingByte});
        }

        return concatBlocks(encryptedBlocks);
    }

    public byte[] decryptBytes(byte[] ciphertext) {
        int blockSize = (p.bitLength() - 1) / 8; // Number of bytes required to represent a block in ElGamal
        int maxBlockSize = getMaxBlockSize(); // Max size of c1 and c2
        List<byte[]> decryptedBlocks = new ArrayList<>();
        int encryptedBlockSize = 2 * maxBlockSize + 2;

        for (int i = 0; i < ciphertext.length; i += encryptedBlockSize) {
            byte[] c1Bytes = Arrays.copyOfRange(ciphertext, i, i + maxBlockSize);
            byte c1PaddingByte = ciphertext[i + maxBlockSize]; // Read padding length byte
            byte[] c2Bytes = Arrays.copyOfRange(ciphertext, i + maxBlockSize + 1, i + 2 * maxBlockSize + 1);
            byte c2PaddingByte = ciphertext[i + 2 * maxBlockSize + 1]; // Read padding length byte

            c1Bytes = Arrays.copyOfRange(c1Bytes, 0, maxBlockSize - (c1PaddingByte & 0xFF));
            c2Bytes = Arrays.copyOfRange(c2Bytes, 0, maxBlockSize - (c2PaddingByte & 0xFF));

            BigInteger[] cipherBlock = new BigInteger[2];
            cipherBlock[0] = new BigInteger(1, c1Bytes);
            cipherBlock[1] = new BigInteger(1, c2Bytes);

            byte[] decrypted = this.decrypt(cipherBlock).toByteArray();
            if(decrypted.length > blockSize && decrypted[0] == 0){
                decrypted = Arrays.copyOfRange(decrypted, 1, decrypted.length);
            }
            else if (decrypted.length < blockSize) {
                byte[] paddedDecrypted = new byte[blockSize];
                System.arraycopy(decrypted, 0, paddedDecrypted, blockSize - decrypted.length, decrypted.length);
                decrypted = paddedDecrypted;
            }
            decryptedBlocks.add(decrypted);
        }
        // Concatenate all decrypted blocks and remove padding
        byte[] decryptedData = concatBlocks(decryptedBlocks);
        return usunPadding(decryptedData);
    }

    private byte[] dodajPadding(byte[] dane, int blockSize) {
        int brakujaceBajty = blockSize - (dane.length % blockSize);
        if (brakujaceBajty == 0) {
            brakujaceBajty = blockSize; // Dodajemy cały nowy blok paddingu
        }
        byte[] daneZPaddingiem = new byte[dane.length + brakujaceBajty];
        System.arraycopy(dane, 0, daneZPaddingiem, 0, dane.length);
        for (int i = dane.length; i < daneZPaddingiem.length; i++) {
            daneZPaddingiem[i] = (byte) brakujaceBajty;
        }
        return daneZPaddingiem;
    }

    private byte[] usunPadding(byte[] dane) {
        if (dane.length == 0) {
            throw new IllegalArgumentException("Invalid padding length");
        }
        int iloscPaddingu = dane[dane.length - 1] & 0xFF;
        if (iloscPaddingu <= 0 || iloscPaddingu > dane.length) {
            throw new IllegalArgumentException("Invalid padding value");
        }
        return Arrays.copyOfRange(dane, 0, dane.length - iloscPaddingu);
    }

    private byte[] padToLength(byte[] bytes, int length) {
        if (bytes.length >= length) {
            return bytes;
        }
        byte[] padded = new byte[length];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    public static List<byte[]> splitIntoBlocks(byte[] array, int blocksize) {
        List<byte[]> blocks = new ArrayList<>();
        int inpos = 0;
        int remaining = array.length;
        while (remaining > 0) {
            int len = Math.min(remaining, blocksize);
            blocks.add(Arrays.copyOfRange(array, inpos, inpos + len));
            inpos += len;
            remaining -= len;
        }
        return blocks;
    }

    public static byte[] concatBlocks(List<byte[]> blocks) {
        int outpos = 0;
        int len = 0;
        for (byte[] b : blocks) {
            len += b.length;
        }
        byte[] res = new byte[len];
        for (byte[] b : blocks) {
            System.arraycopy(b, 0, res, outpos, b.length);
            outpos += b.length;
        }
        return res;
    }


    // Metoda do szyfrowania pliku
    public void encryptFile(String inputFile, String outputFile) throws IOException {
        byte[] fileData = Files.readAllBytes(Paths.get(inputFile));
        byte[] encryptedData = encryptBytes(fileData);
        Files.write(Paths.get(outputFile), encryptedData);
    }

    // Metoda do deszyfrowania pliku
    public void decryptFile(String inputFile, String outputFile) throws IOException {
        byte[] fileData = Files.readAllBytes(Paths.get(inputFile));
        byte[] decryptedData = decryptBytes(fileData);
        Files.write(Paths.get(outputFile), decryptedData);
    }

    public boolean millerRabinTest(BigInteger n, int k) {
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) {
            return true;
        }
        if (n.compareTo(BigInteger.TWO) < 0 || n.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            return false;
        }

        BigInteger s = BigInteger.ZERO;
        BigInteger d = n.subtract(BigInteger.ONE);

        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            d = d.divide(BigInteger.TWO);
            s = s.add(BigInteger.ONE);
        }

        for (int i = 0; i < k; i++) {
            BigInteger a = uniformRandom(BigInteger.TWO, n.subtract(BigInteger.ONE));
            BigInteger x = a.modPow(d, n);

            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) {
                continue;
            }

            boolean continueLoop = false;
            for (BigInteger r = BigInteger.ONE; r.compareTo(s.subtract(BigInteger.ONE)) < 0; r = r.add(BigInteger.ONE)) {
                x = x.modPow(BigInteger.TWO, n);
                if (x.equals(BigInteger.ONE)) {
                    return false;
                }
                if (x.equals(n.subtract(BigInteger.ONE))) {
                    continueLoop = true;
                    break;
                }
            }

            if (!continueLoop) {
                return false;
            }
        }
        return true;
    }

    private BigInteger uniformRandom(BigInteger bottom, BigInteger top) {
        BigInteger res;
        do {
            res = new BigInteger(top.bitLength(), random);
        } while (res.compareTo(bottom) < 0 || res.compareTo(top) > 0);
        return res;
    }
}