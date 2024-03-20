package source;

public class DES {
    byte subKeys[][];
    byte shift[] = {1, 3, 5, 7, 0, 2, 4, 6};
    final byte[] sBox =
    {
            //----------------------------------------------------S1
            14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7,
            0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8,
            4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0,
            15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13,
            //----------------------------------------------------S2
            15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10,
            3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5,
            0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15,
            13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9,
            //----------------------------------------------------S3
            10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8,
            13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1,
            13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7,
            1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12,
            //----------------------------------------------------S4
            7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15,
            13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9,
            10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4,
            3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14,
            //----------------------------------------------------S5
            2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9,
            14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6,
            4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14,
            11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3,
            //----------------------------------------------------S6
            12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11,
            10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8,
            9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6,
            4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13,
            //----------------------------------------------------S7
            4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1,
            13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6,
            1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2,
            6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12,
            //----------------------------------------------------S8
            13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7,
            1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2,
            7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8,
            2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11
            };

    private byte[] encrypt(byte[] text) {

        byte[] left = getLeftBlock(text);
        byte[] right = getRightBlock(text);

        byte[] result;

        // 16 rund kodowania
        for (int i = 0; i < 16; i++){
            byte[] previousRight = right;
            right = getExtendedBlock(right);
            right = XORBytes(right, subKeys[i]);
            //Kolejne kroki !!!!!

            left = previousRight;
        }

        result = combineBlocks(left, right);
        return result;
    }



    //Wyznaczanie lewego bloku
    private byte[] getLeftBlock(byte[] block)
    {
        byte[] result = new byte[4];
        byte current;
        //Iteracja przez bajty
        for (byte byteCounter = 4; byteCounter < 8; byteCounter++)
        {
            //Iteracja przez bity
            for (byte bitCounter = 7; bitCounter >= 0; bitCounter--)
            {
                //Przesunięcie bitów za pomocą shift
                current = (byte) (block[bitCounter] >>> shift[byteCounter]);
                current = (byte) (current & 1);
                current = (byte) (current << (bitCounter));
                //Ustawienie nowego bitu
                result[7 - byteCounter] = (byte) (result[7 - byteCounter] | current);
            }
        }
        return result;
    }

    //Wyznaczanie prawego bloku
    private byte[] getRightBlock(byte[] block)
    {
        byte[] result = new byte[4];
        byte current;
        //Iteracja przez bajty
        for (byte byteCounter = 0; byteCounter < 4; byteCounter++)
        {
            //Iteracja przez bity
            for (byte bitCounter = 7; bitCounter >= 0; bitCounter--)
            {
                //Przesunięcie bitów za pomocą shift
                current = (byte) (block[bitCounter] >>> shift[byteCounter]);
                current = (byte) (current & 1);
                current = (byte) (current << (bitCounter));
                //Ustawienie nowego bitu
                result[3 - byteCounter] = (byte) (result[3 - byteCounter] | current);
            }
        }
        return result;
    }

    //Wyznaczenie rozszerzonego bloku
    private byte[] getExtendedBlock(byte[] block)
    {
        byte extendedBlock[] = new byte[6];
        short current;
        byte pBit = 31;
        byte changer = 0;

        for (int bit = 0; bit < 48; bit++)
        {
            //Pobranie aktualnego bitu z bloku danych wejściowych
            current = (short) (block[pBit / 8] >> (7 - (pBit % 8)));
            //Pozostawienie jedynie najmniej znaczącego bitu
            current = (short) (current & 1);
            //Przesunięcie bitu na odpowiednią pozycję w bajcie wyjściowym
            current = (short) (current << (7 - (bit % 8)));
            //Umieszczenie przetworzonego bitu w odpowiednim miejscu w bloku danych wyjściowych
            extendedBlock[bit / 8] = (byte) (extendedBlock[bit / 8] | (current));

            //Sprawdzenie, czy przetworzono już 6 bitów
            if (++changer == 6)
            {
                changer = 0;
                pBit--;
            }
            else
            {
                pBit = (byte) ((++pBit) % 32);
            }
        }
        return extendedBlock;
    }

    //Xorowanie dwóch tablic
    public static byte[] XORBytes(byte[] a, byte[] b)
    {
        byte[] result = new byte[a.length];

        for (int i = 0; i < a.length; i++)
        {
            result[i] = (byte) (a[i] ^ b[i]);
        }

        return result;
    }

    //Łączenie dwóch bloków w jeden
    private byte[] combineBlocks(byte[] left, byte[] right)
    {
        //Tworzenie tablicy, która będzie przechowywać dane wejściowe
        byte[] data = new byte[8];
        //Tworzenie tablicy, która będzie przechowywać wynikowe dane
        byte[] result = new byte[8];
        //Zmienna przechowująca aktualnie przetwarzany bit
        byte current;

        //Kopiowanie danych lewej połowy do tablicy danych, zaczynając od 4. bajtu
        System.arraycopy(left, 0, data, 4, 4);
        //Kopiowanie danych prawej połowy do tablicy danych, zaczynając od 0. bajtu
        System.arraycopy(right, 0, data, 0, 4);

        //Licznik bitów w wynikowym bloku
        int currBit = 0;

        for (int readBitPos = 7; readBitPos >= 0; readBitPos--)
        {
            for (int readBytePos = 4; readBytePos < 8; readBytePos++)
            {
                //Pobieranie aktualnego bitu z danych
                current = (byte) (data[readBytePos] >> (7 - (readBitPos)));
                //Pozostawienie tylko najmniej znaczącego bitu
                current = (byte) (current & 1);
                //Umieszczenie bitu w odpowiedniej pozycji w wynikowej tablicy
                current = (byte) (current << (7 - (currBit % 8)));
                result[currBit / 8] = (byte) (result[currBit / 8] | current);
                //Przesunięcie o 2 pozycje, ponieważ wynikowy blok ma 8 bajtów
                currBit += 2;
            }
        }

        currBit = 1;

        for (int readBitPos = 7; readBitPos >= 0; readBitPos--)
        {
            for (int readBytePos = 0; readBytePos < 4; readBytePos++)
            {
                //Pobieranie aktualnego bitu z danych
                current = (byte) (data[readBytePos] >> (7 - (readBitPos)));
                //Pozostawienie tylko najmniej znaczącego bitu
                current = (byte) (current & 1);
                //Umieszczenie bitu w odpowiedniej pozycji w wynikowej tablicy
                current = (byte) (current << (7 - (currBit % 8)));
                result[currBit / 8] = (byte) (result[currBit / 8] | current);
                //Przesunięcie o 2 pozycje, ponieważ wynikowy blok ma 8 bajtów
                currBit += 2;
            }
        }

        return result;
    }


}
