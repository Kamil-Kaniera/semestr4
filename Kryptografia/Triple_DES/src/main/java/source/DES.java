package source;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;

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

    private final byte[][] SBox = {

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

    private byte[][] dzielNaGrupy(byte[] zrodlo, int ileWGrupie) {
        int ileGrup = (int)Math.ceil((double)zrodlo.length / (double)ileWGrupie);
        byte[][] wynik = new byte[ileGrup][];
        int temp = 0;

        for (int i = 0; i < ileGrup; i++) {
            wynik[i] = new byte[ileWGrupie];
            for (int j = 0; j < ileWGrupie; j++) {
                if (i != ileGrup - 1) {
                    wynik[i][j] = zrodlo[i * ileWGrupie + j];
                } else {
                    int ileLewych = zrodlo.length - i * ileWGrupie;
                    if (j < ileWGrupie - ileLewych) {
                        wynik[i][j] = 0;
                    } else {
                        wynik[i][j] = zrodlo[i * ileWGrupie + temp++];
                    }
                }
            }
        }

        return wynik;
    }

    private int doInta(byte[] zrodlo) {
        int wynik = 0;
        int temp = 1;

        for (int i = zrodlo.length - 1; i >= 0; i--) {
            // Konwersja wartości bajtowej na wartość całkowitą bez znaku
            int bezZnaku = zrodlo[i] & 0xff;
            wynik += bezZnaku * temp;
            temp *= 2;
        }
        return wynik;
    }

    private byte[] doBitow(int zrodlo, int ileBitow) {
        byte[] wynik = new byte[ileBitow];
        int i = 0;
        int temp = zrodlo;

        while (temp != 0) {
            wynik[i++] = (byte)(temp % 2);
            temp /= 2;
        }

        // Odwracanie tablicy wynik
        byte[] odwroconyWynik = new byte[wynik.length];
        for (int j = 0; j < wynik.length; j++) {
            odwroconyWynik[j] = wynik[wynik.length - 1 - j];
        }

        return odwroconyWynik;
    }

    private byte[] OperacjaXOR(byte[] jeden, byte[] dwa) {
        byte[] xor = new byte[jeden.length];
        for (int i = 0; i < xor.length; i++) {
            if (jeden[i] == dwa[i]) {
                xor[i] = 0;
            } else {
                xor[i] = 1;
            }
        }

        return xor;
    }

    private byte[] permutuj(byte[] oryginalny, byte[] permutacja) {
        byte[] permutowane = new byte[permutacja.length];

        for (int i = 0; i < permutacja.length; i++) {
            permutowane[i] = oryginalny[permutacja[i] - 1];
        }

        return permutowane;
    }

    private byte[] concat(byte[] lewa, byte[] prawa) {
        byte[] konkatenacja = new byte[lewa.length + prawa.length];
        int j = 0;
        for (int i = 0; i < lewa.length; i++, j++) {
            konkatenacja[j] = lewa[i];
        }
        for (int i = 0; i < prawa.length; i++, j++) {
            konkatenacja[j] = prawa[i];
        }

        return konkatenacja;
    }

    private byte[] przesunLewo(byte[] przesuniecie) {
        byte pierwszy = przesuniecie[0];
        byte[] przesuniety = new byte[przesuniecie.length];

        for (int i = 0; i < przesuniecie.length - 1; i++) {
            przesuniety[i] = przesuniecie[i + 1];
        }
        przesuniety[przesuniecie.length - 1] = pierwszy;

        return przesuniety;
    }

    private byte[] przesunLewo(byte[] przesuniecie, byte liczba) {
        byte[] przesuniety = new byte[przesuniecie.length];
        System.arraycopy(przesuniecie, 0, przesuniety, 0, przesuniecie.length);
        for (int i = 0; i < liczba; i++) {
            przesuniety = przesunLewo(przesuniety);
        }

        return przesuniety;
    }

    private byte[] lewaPolowa(byte[] zrodlo) {
        int dlugosc = zrodlo.length / 2;
        byte[] lewaStrona = new byte[dlugosc];

        for (int i = 0; i < dlugosc; i++) {
            lewaStrona[i] = zrodlo[i];
        }

        return lewaStrona;
    }

    private byte[] prawaPolowa(byte[] zrodlo) {
        int dlugosc = zrodlo.length / 2;
        byte[] prawaStrona = new byte[dlugosc];

        for (int i = 0; i < dlugosc; i++) {
            prawaStrona[i] = zrodlo[dlugosc + i];
        }

        return prawaStrona;
    }

    private byte[][] generujPodKlucze(byte[] originalnyKlucz) {
        byte[] permutowanyKlucz = permutuj(originalnyKlucz, PC1);
        byte[] temp;
        byte[][] podKlucze = new byte[16][];
        byte[] prawyKlucz = prawaPolowa(permutowanyKlucz);
        byte[] lewyKlucz = lewaPolowa(permutowanyKlucz);

        for (int i = 0; i < 16; i++) {
            lewyKlucz = przesunLewo(lewyKlucz, ShiftKey[i]);
            prawyKlucz = przesunLewo(prawyKlucz, ShiftKey[i]);
            temp = concat(lewyKlucz, prawyKlucz);
            podKlucze[i] = permutuj(temp, PC2);
        }

        return podKlucze;
    }

    private int dostanNumer(byte[] zrodlo) {
        byte[] kolumna = { zrodlo[1], zrodlo[2], zrodlo[3], zrodlo[4] };
        byte[] linia = { zrodlo[0], zrodlo[5] };

        return doInta(linia) * 16 + doInta(kolumna);
    }

    private byte[] funkcjaFeistela(byte[] prawo, byte[] klucz) {
        byte[] wynik = new byte[0];
        byte[] prawoPermut = permutuj(prawo, EX);
        byte[] xor = OperacjaXOR(prawoPermut, klucz);
        byte[][] grupy = dzielNaGrupy(xor, 6);

        for (int i = 0; i < grupy.length; i++) {
            wynik = concat(wynik, doBitow(SBox[i][dostanNumer(grupy[i])], 4));
        }
        wynik = permutuj(wynik, PBLOCK);

        return wynik;
    }

    private byte[] wykonujIteracje(byte[] lewa, byte[] prawa, byte[][] podKlucze) {
        byte[] poprzedniaLewa = new byte[lewa.length];
        for (int i = 0; i < 16; i++) {
            System.arraycopy(lewa, 0, poprzedniaLewa, 0, lewa.length);
            System.arraycopy(prawa, 0, lewa, 0, prawa.length);
            prawa = OperacjaXOR(poprzedniaLewa, funkcjaFeistela(prawa, podKlucze[i]));
        }
        byte[] wynik = concat(prawa, lewa);
        wynik = permutuj(wynik, FP);

        return wynik;
    }

    /*########### METODY PUBLICZNE ##########*/

    public byte[] szyfruj(byte[] wiadomosc, byte[] klucz, boolean szyfrowanie) {
        byte[][] podKlucze = generujPodKlucze(klucz);
        if (!szyfrowanie) {
            for (int i = 0; i < podKlucze.length / 2; i++) {
                byte[] temp = podKlucze[i];
                podKlucze[i] = podKlucze[podKlucze.length - 1 - i];
                podKlucze[podKlucze.length - 1 - i] = temp;
            }
        }

        byte[] poczatkowaPerm = permutuj(wiadomosc, IP);
        byte[] lewaTab = lewaPolowa(poczatkowaPerm);
        byte[] prawaTab = prawaPolowa(poczatkowaPerm);
        byte[] cipherText = wykonujIteracje(lewaTab, prawaTab, podKlucze);

        return cipherText;
    }

//    public byte[] szyfrujTabBajtow(byte[] wiadomosc, byte[] klucz, boolean szyfrowanie) {
//        byte[] wynik = new byte[0];
//        byte[] cipherText = new byte[0];
//        byte[] cipherBity = new byte[0];
//        byte[] doSzyfrowania = new byte[0];
//
//        for (int i = 0; i < wiadomosc.length; i++) {
//            doSzyfrowania = concat(doSzyfrowania, doBitow(wiadomosc[i], 8));
//        }
//
//        byte[][] grupa64Bitow = dzielNaGrupy(doSzyfrowania, 64);
//
//        for (int i = 0; i < grupa64Bitow.length; i++) {
//            cipherBity = concat(cipherBity, szyfruj(grupa64Bitow[i], klucz, szyfrowanie));
//        }
//
//        byte[][] grupa8Bitow = dzielNaGrupy(cipherBity, 8);
//
//        for (var group : grupa8Bitow) {
//            wynik = concat(wynik, new byte[] { (byte) doInta(group) });
//        }
//
//        return wynik;
//    }

    public String szyfruj(String wiadomosc, byte[] klucz, boolean szyfrowanie) {
        byte[] bity = wiadomosc.getBytes(StandardCharsets.ISO_8859_1);
        String wynik = "";
        byte[] cipherText = new byte[0];
        byte[] cipherBity = new byte[0];
        byte[] doSzyfrowania = new byte[0];

        if (!szyfrowanie) {
            bity = Base64.getDecoder().decode(wiadomosc);
        }

        for (int i = 0; i < bity.length; i++) {
            doSzyfrowania = concat(doSzyfrowania, doBitow(bity[i], 8));
        }

        byte[][] grupa64Bitow = dzielNaGrupy(doSzyfrowania, 64);

        for (int i = 0; i < grupa64Bitow.length; i++) {
            cipherBity = concat(cipherBity, szyfruj(grupa64Bitow[i], klucz, szyfrowanie));
        }

        byte[][] grupa8Bitow = dzielNaGrupy(cipherBity, 8);
        ArrayList<Byte> kodowanoDoLanchucha = new ArrayList<>();

        for (byte[] group : grupa8Bitow) {
            if (szyfrowanie) {
                StringBuilder sb = new StringBuilder();
                for (byte bit : group) {
                    sb.append(bit);
                }
                String joinedBits = sb.toString();
                kodowanoDoLanchucha.add((byte) Integer.parseInt(joinedBits, 2));
            }
            else{
                wynik += doInta(group);
            }
        }

        if (szyfrowanie) {
            byte[] wynikBytes = new byte[kodowanoDoLanchucha.size()];
            for (int i = 0; i < wynikBytes.length; i++) {
                wynikBytes[i] = kodowanoDoLanchucha.get(i);
            }
            return Base64.getEncoder().encodeToString(wynikBytes);
        }

        return wynik;
    }

}