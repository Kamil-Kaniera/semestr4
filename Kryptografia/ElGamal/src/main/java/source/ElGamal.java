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

    public byte[] encryptBytes(byte[] data) {
        List<BigInteger[]> encryptedBlocks = new ArrayList<>();
        int minBlockSize = 1; // Minimalny rozmiar bloku w bajtach

        int bitLength = p.bitLength();
        int blockSize = Math.max(minBlockSize, (bitLength - 1) / 8); // Block size in bytes

        for (int i = 0; i < data.length; i += blockSize) {
            int length = Math.min(blockSize, data.length - i);
            byte[] block = new byte[length];
            System.arraycopy(data, i, block, 0, length);
            BigInteger message = new BigInteger(1, block); // Convert block to BigInteger
            encryptedBlocks.add(encrypt(message));
        }

        List<Byte> encryptedBytes = new ArrayList<>();
        for (BigInteger[] encryptedBlock : encryptedBlocks) {
            byte[] c1Bytes = encryptedBlock[0].toByteArray();
            byte[] c2Bytes = encryptedBlock[1].toByteArray();
            addLengthAndData(encryptedBytes, c1Bytes);
            addLengthAndData(encryptedBytes, c2Bytes);
        }

        byte[] result = new byte[encryptedBytes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = encryptedBytes.get(i);
        }
        return result;
    }



    public byte[] decryptBytes(byte[] data) {
        List<byte[]> decryptedBlocks = new ArrayList<>();

        int bitLength = p.bitLength();
        int minBlockSize = 1; // Minimalny rozmiar bloku w bajtach
        int blockSize = Math.max(minBlockSize, (bitLength - 1) / 8); // Oblicz długość bloku w bajtach

        BigInteger[] ciphertext =  bytesToBigIntegers(data, blockSize);
        BigInteger decrypted = decrypt(ciphertext);
        return decrypted.toByteArray();
    }

    public BigInteger[] bytesToBigIntegers(byte[] data, int blockSize) {
        List<BigInteger> bigIntegers = new ArrayList<>();

        for (int i = 0; i < data.length; i += blockSize) {
            int length = Math.min(blockSize, data.length - i);
            byte[] block = Arrays.copyOfRange(data, i, i + length);
            bigIntegers.add(new BigInteger(block));
        }

        return bigIntegers.toArray(new BigInteger[0]);
    }


    private void addLengthAndData(List<Byte> byteList, byte[] data) {
        byte[] lengthBytes = BigInteger.valueOf(data.length).toByteArray();
//        for (byte b : lengthBytes) {
//            byteList.add(b);
//        }
        for (byte b : data) {
            byteList.add(b);
        }
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
