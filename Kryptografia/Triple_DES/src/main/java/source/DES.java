package source;

import java.util.Arrays;

public class DES {
    private final byte[] IP = {
            58, 50, 42, 34, 26, 18, 10, 2,
            60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6,
            64, 56, 48, 40, 32, 24, 16, 8,
            57, 49, 41, 33, 25, 17, 9, 1,
            59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7};

    private final byte[] FP = {
            40, 8, 48, 16, 56, 24, 64, 32,
            39, 7, 47, 15, 55, 23, 63, 31,
            38, 6, 46, 14, 54, 22, 62, 30,
            37, 5, 45, 13, 53, 21, 61, 29,
            36, 4, 44, 12, 52, 20, 60, 28,
            35, 3, 43, 11, 51, 19, 59, 27,
            34, 2, 42, 10, 50, 18, 58, 26,
            33, 1, 41, 9, 49, 17, 57, 25};

    private final byte[] PC1 = {
            57, 49, 41, 33, 25, 17, 9,
            1, 58, 50, 42, 34, 26, 18,
            10, 2, 59, 51, 43, 35, 27,
            19, 11, 3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15,
            7, 62, 54, 46, 38, 30, 22,
            14, 6, 61, 53, 45, 37, 29,
            21, 13, 5, 28, 20, 12, 4};

    private final byte[] PC2 = {
            14, 17, 11, 24, 1, 5,
            3, 28, 15, 6, 21, 10,
            23, 19, 12, 4, 26, 8,
            16, 7, 27, 20, 13, 2,
            41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53,
            46, 42, 50, 36, 29, 32};

    private final byte[] EX = {
            32, 1, 2, 3, 4, 5,
            4, 5, 6, 7, 8, 9,
            8, 9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32, 1};

    private final byte[] PBLOCK = {
            16, 7, 20, 21,
            29, 12, 28, 17,
            1, 15, 23, 26,
            5, 18, 31, 10,
            2, 8, 24, 14,
            32, 27, 3, 9,
            19, 13, 30, 6,
            22, 11, 4, 25};

    private final int[][] SBox = {

                    {14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
                    0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
                    4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
                    15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13},

                    {15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
                    3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
                    0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
                    13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9},

                    { 10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
                    13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
                    13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
                    1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12},

                    {7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
                    13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
                    10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
                    3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14},

                    {2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
                    14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
                    4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
                    11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3},

                    {12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
                    10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
                    9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
                    4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13},

                    {4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
                    13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
                    1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
                    6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12},

                    {13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
                    1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
                    7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
                    2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}};

    private final byte[] ShiftKey = {1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};


    ///////////////////////////////////////////////////////////////////////////////////////////////////////


    public  byte[] szyfruj(byte[] wiadomosc, byte[] klucz, boolean szyfruj) {
        int blockSize = 8; // 8 bajtów dla DES
        int numOfBlocks = (wiadomosc.length + blockSize - 1) / blockSize;

        byte[] result = new byte[wiadomosc.length];

        for (int i = 0; i < numOfBlocks; i++) {
            byte[] block = Arrays.copyOfRange(wiadomosc, i * blockSize, (i + 1) * blockSize);
            byte[] processedBlock = szyfrujBlok(block, klucz, szyfruj);
            System.arraycopy(processedBlock, 0, result, i * blockSize, blockSize);
        }

        return result;
    }

    public byte[] szyfrujBlok(byte[] blok, byte[] klucz, boolean szyfruj) {
        if (szyfruj) {
            return encrypt(blok, klucz);
        } else {
            return decrypt(blok, klucz);
        }
    }

    public byte[] encrypt(byte[] plaintext, byte[] key) {
        byte[][] subKeys = generateSubKeys(key);
        byte[] ip = permute(plaintext, IP);

        byte[] left = new byte[4];
        byte[] right = new byte[4];
        System.arraycopy(ip, 0, left, 0, 4);
        System.arraycopy(ip, 4, right, 0, 4);

        for (int i = 0; i < 16; i++) {
            byte[] temp = right;
            right = xor(left, feistel(right, subKeys[i]));
            left = temp;
        }

        byte[] ciphertext = concat(right, left);
        return permute(ciphertext, FP);
    }

    public  byte[] decrypt(byte[] ciphertext, byte[] key) {
        byte[][] subKeys = generateSubKeys(key);
        byte[] ip = permute(ciphertext, IP);

        byte[] left = new byte[4];
        byte[] right = new byte[4];
        System.arraycopy(ip, 0, left, 0, 4);
        System.arraycopy(ip, 4, right, 0, 4);

        for (int i = 15; i >= 0; i--) {
            byte[] temp = right;
            right = xor(left, feistel(right, subKeys[i]));
            left = temp;
        }

        byte[] plaintext = concat(right, left);
        return permute(plaintext, FP);
    }

    private byte[] feistel(byte[] right, byte[] subKey) {
        byte[] expanded = permute(right, EX);
        byte[] xored = xor(expanded, subKey);
        byte[] substituted = substitute(xored);
        return permute(substituted, PBLOCK);
    }

    private byte[] permute(byte[] input, byte[] table) {
        byte[] output = new byte[table.length / 8];
        for (int i = 0; i < table.length; i++) {
            int bit = extractBit(input, table[i] - 1);
            setBit(output, i, bit);
        }
        return output;
    }

    private byte[] substitute(byte[] input) {
        byte[] output = new byte[4];
        for (int i = 0; i < 8; i++) {
            int row = 2 * extractBit(input, 6 * i) + extractBit(input, 6 * i + 5);
            int col = 8 * extractBit(input, 6 * i + 1) + 4 * extractBit(input, 6 * i + 2) +
                    2 * extractBit(input, 6 * i + 3) + extractBit(input, 6 * i + 4);
            int value = SBox[i][16 * row + col];
            for (int j = 0; j < 4; j++) {
                setBit(output, 4 * i + j, (value >> (3 - j)) & 1);
            }
        }
        return output;
    }

    private byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    private byte[] rotLeft(byte[] input, int len, int round) {
        int nrBytes = (len - 1) / 8 + 1;
        byte[] out = new byte[nrBytes];
        for (int i = 0; i < len; i++) {
            int val = extractBit(input, (i + round) % len);
            setBit(out, i, val);
        }
        return out;
    }
    
    private int extractBit(byte[] data, int position) {
        int index = position / 8;
        int bitPosition = 7 - (position % 8);
        return (data[index] >> bitPosition) & 1;
    }

    private byte[] extractBits(byte[] input, int position, int n) {
        int numOfBytes = (n - 1) / 8 + 1;
        byte[] out = new byte[numOfBytes];
        for (int i = 0; i < n; i++) {
            int val = extractBit(input, position + i);
            setBit(out, i, val);
        }
        return out;

    }

    private byte[][] generateSubKeys(byte[] key) {
        byte[][] tmp = new byte[16][];
        byte[] tmpK = permute(key, PC1);

        byte[] C = extractBits(tmpK, 0, PC1.length/2);
        byte[] D = extractBits(tmpK, PC1.length/2, PC1.length/2);

        for (int i = 0; i < 16; i++) {

            C = rotLeft(C, 28, ShiftKey[i]);
            D = rotLeft(D, 28, ShiftKey[i]);

            byte[] cd = concatBits(C, 28, D, 28);

            tmp[i] = permute(cd, PC2);
        }

        return tmp;
    }

    private byte[] concatBits(byte[] a, int aLen, byte[] b, int bLen) {
        int numOfBytes = (aLen + bLen - 1) / 8 + 1;
        byte[] out = new byte[numOfBytes];
        int j = 0;
        for (int i = 0; i < aLen; i++) {
            int val = extractBit(a, i);
            setBit(out, j, val);
            j++;
        }
        for (int i = 0; i < bLen; i++) {
            int val = extractBit(b, i);
            setBit(out, j, val);
            j++;
        }
        return out;
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private void setBit(byte[] array, int position, int value) {
        int index = position / 8;
        int bitPosition = 7 - (position % 8);
        if (value == 1) {
            array[index] |= (1 << bitPosition);
        } else {
            array[index] &= ~(1 << bitPosition);
        }
    }

}
