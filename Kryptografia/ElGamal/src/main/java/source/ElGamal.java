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

        List<byte[]> blocks = splitIntoBlocks(message, blockSize);

        for (byte[] block : blocks) {
            // Padding
            if (block.length < blockSize) {
                block = Arrays.copyOf(block, blockSize);
            }

            BigInteger messageBlock = new BigInteger(1, block);
            BigInteger[] encryptedBlock = encrypt(messageBlock);

            byte[] c1Bytes = encryptedBlock[0].toByteArray();
            byte[] c2Bytes = encryptedBlock[1].toByteArray();

            // Pad c1 and c2 to maxBlockSize
            c1Bytes = padToLength(c1Bytes, maxBlockSize);
            c2Bytes = padToLength(c2Bytes, maxBlockSize);

            encryptedBlocks.add(c1Bytes);
            encryptedBlocks.add(c2Bytes);
        }

        return concatBlocks(encryptedBlocks);
    }

    public byte[] decryptBytes(byte[] ciphertext) {
        int maxBlockSize = getMaxBlockSize(); // Max size of c1 and c2
        List<byte[]> decryptedBlocks = new ArrayList<>();
        int encryptedBlockSize = 2 * maxBlockSize;

        for (int i = 0; i < ciphertext.length; i += encryptedBlockSize) {
            byte[] c1Bytes = Arrays.copyOfRange(ciphertext, i, i + maxBlockSize);
            byte[] c2Bytes = Arrays.copyOfRange(ciphertext, i + maxBlockSize, i + encryptedBlockSize);

            BigInteger[] cipherBlock = new BigInteger[2];
            cipherBlock[0] = new BigInteger(1, c1Bytes);
            cipherBlock[1] = new BigInteger(1, c2Bytes);

            byte[] decrypted = this.decrypt(cipherBlock).toByteArray();
            byte[] unpaddedDecrypted = removePadding(decrypted);
            decryptedBlocks.add(unpaddedDecrypted);
        }

        return concatBlocks(decryptedBlocks);
    }

    private byte[] removePadding(byte[] bytes) {
        int i = bytes.length;
        while (i > 0 && bytes[i - 1] == 0) {
            i--;
        }
        return Arrays.copyOf(bytes, i);
    }

    private byte[] padToLength(byte[] bytes, int length) {
        if (bytes.length >= length) {
            return bytes;
        }
        byte[] padded = new byte[length];
        System.arraycopy(bytes, 0, padded, length - bytes.length, bytes.length);
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