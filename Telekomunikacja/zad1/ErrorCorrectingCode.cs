using System;
using System.IO;
using System.Text;

namespace zad1
{
    public class ErrorCorrectingCode
    {
        private const int ByteInBits = 8;

        // Kodowanie wiadomości
        public void Coding(int[,] matrixH, string inputFileName, string encodedFileName, string encodedStringFileName)
        {
            using (StreamReader input = new StreamReader(inputFileName, Encoding.UTF8))
            using (StreamWriter encoded = new StreamWriter(encodedFileName, false, Encoding.UTF8))
            using (FileStream code2 = new FileStream(encodedStringFileName, FileMode.Create))
            {
                while (true)
                {
                    int symbol = input.Read();
                    if (symbol == -1)
                        break;

                    int[] message = new int[ByteInBits];
                    int[] tableControl = new int[ByteInBits];

                    // Konwersja symbolu na binarny ciąg bitów
                    for (int i = ByteInBits - 1; i >= 0; i--)
                    {
                        message[i] = symbol % 2;
                        symbol /= 2;
                    }

                    // Obliczenie kontrolnego ciągu bitów
                    for (int i = 0; i < ByteInBits; i++)
                    {
                        for (int j = 0; j < ByteInBits; j++)
                        {
                            tableControl[i] += message[j] * matrixH[i, j];
                        }

                        tableControl[i] %= 2;
                    }

                    // Zapisanie zakodowanej wiadomości do pliku
                    for (int i = 0; i < ByteInBits; i++)
                    {
                        encoded.Write(message[i]);
                    }

                    // Zapisanie kontrolnego ciągu bitów do pliku
                    for (int i = 0; i < ByteInBits; i++)
                    {
                        encoded.Write(tableControl[i]);
                    }

                    encoded.WriteLine();

                    // Zapisz dwa kody dla każdego bajtu danych
                    int a = 128;
                    byte[] kod = new byte[2];

                    // Konwersja binarnego ciągu bitów na symbol i zapis do pliku
                    for (int i = 0; i < 8; i++)
                    {
                        kod[0] += (byte)(a * (i < ByteInBits ? message[i] : 0));
                        kod[1] += (byte)(a * (i < ByteInBits ? tableControl[i] : 0));
                        a /= 2;
                    }

                    code2.WriteByte(kod[0]);
                    code2.WriteByte(kod[1]);

                }
            }
        }
        
        public void Checking(int[,] matrixH, string encodedFileName, string decodedFileName)
        {
            using (FileStream encoded = new FileStream(encodedFileName, FileMode.Open))
            using (StreamWriter decoded = new StreamWriter(decodedFileName, false))
            {
                int[] encodedArray = new int[ByteInBits * 2];
                int[] errorArray = new int[ByteInBits];
                int counter = 0;
                int errorCounter = 0;

                int[] encodedBytes = new int[2];
                while (true)
                {
                    encodedBytes[counter] = encoded.ReadByte();
                    if (encodedBytes[counter] == -1)
                        break;
                    
                    counter++;
                    
                   
                    if (counter == 2)
                    {
                        // Funkcja do konwersji int na bit
                        Func<int, int[]> IntToBits = (value) =>
                        {
                            int[] bits = new int[ByteInBits];
                            for (int i = ByteInBits - 1; i >= 0; i--)
                            {
                                bits[i] = value % 2;
                                value /= 2;
                            }
                            return bits;
                        };

                        int[] bits1 = IntToBits(encodedBytes[0]);
                        int[] bits2 = IntToBits(encodedBytes[1]);

                        Array.Copy(bits1, encodedArray, ByteInBits);
                        Array.Copy(bits2, 0, encodedArray, ByteInBits, ByteInBits);

                        // Obliczenie kontrolnego ciągu bitów dla otrzymanych danych
                        for (int i = 0; i < ByteInBits; i++)
                        {
                            errorArray[i] = 0;
                            for (int j = 0; j < ByteInBits * 2; j++)
                            {
                                errorArray[i] += encodedArray[j] * matrixH[i, j];
                            }
                            errorArray[i] %= 2;

                            if (errorArray[i] == 1)
                            {
                                errorCounter = 1;
                            }
                        }

                        // Sprawdzenie i naprawa błędów
                        if (errorCounter != 0)
                        {
                            int exists = 0;
                            for (int i = 0; i < 15; i++)
                            {
                                for (int j = i + 1; j < ByteInBits * 2; j++)
                                {
                                    exists = 1;
                                    for (int k = 0; k < ByteInBits; k++)
                                    {
                                        if (errorArray[k] != (matrixH[k, i] ^ matrixH[k, j]))
                                        {
                                            exists = 0;
                                            break;
                                        }
                                    }

                                    if (exists == 1)
                                    {
                                        int firstErrorBit = i;
                                        int secondErrorBit = j;
                                        encodedArray[firstErrorBit] = encodedArray[firstErrorBit] == 0 ? 1 : 0;
                                        encodedArray[secondErrorBit] = encodedArray[secondErrorBit] == 0 ? 1 : 0;
                                        i = ByteInBits * 2;
                                        break;
                                    }
                                }
                            }

                            if (errorCounter == 1)
                            {
                                for (int i = 0; i < ByteInBits * 2; i++)
                                {
                                    int j;
                                    for (j = 0; j < ByteInBits; j++)
                                    {
                                        if (matrixH[j, i] != errorArray[j])
                                        {
                                            break;
                                        }
                                    }

                                    if (j == ByteInBits)
                                    {
                                        encodedArray[i] = encodedArray[i] == 0 ? 1 : 0;
                                        i = ByteInBits * 2;
                                    }
                                }
                            }
                        }

                        counter = 0;
                        errorCounter = 0;
                        
                        // Konwersja ciągu bitów na symbol i zapis do pliku
                        int a = 128;
                        int kod = 0;
                        for (int i = 0; i < ByteInBits; i++)
                        {
                            kod += a * encodedArray[i];
                            a /= 2;
                        }
                        decoded.Write((char)kod);
                    }
                }
                Console.WriteLine("\nPlik odkodowany poprawnie");
            }
        }
    
    }
}
