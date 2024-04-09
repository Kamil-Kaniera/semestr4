﻿using System;
using System.IO;
using System.Text;

public class Code
{
    private const int ByteInBits = 8;

    public void Coding(int[,] matrixH, string inputFileName, string encodedFileName, string code2FileName)
    {
        using (StreamReader input = new StreamReader(inputFileName, Encoding.UTF8))
        using (StreamWriter encoded = new StreamWriter(encodedFileName, false, Encoding.UTF8))
        using (FileStream code2 = new FileStream(code2FileName, FileMode.Create))
        {
            while (true)
            {
                int symbol = input.Read();
                if (symbol == -1)
                    break;

                int[] message = new int[ByteInBits];
                int[] tableControl = new int[ByteInBits];

                for (int i = ByteInBits - 1; i >= 0; i--)
                {
                    message[i] = symbol % 2;
                    symbol /= 2;
                }

                for (int i = 0; i < ByteInBits; i++)
                {
                    for (int j = 0; j < ByteInBits; j++)
                    {
                        tableControl[i] += message[j] * matrixH[i, j];
                    }
                    tableControl[i] %= 2;
                }

                for (int i = 0; i < ByteInBits; i++)
                {
                    encoded.Write(message[i]);
                }

                for (int i = 0; i < ByteInBits; i++)
                {
                    encoded.Write(tableControl[i]);
                }

                encoded.WriteLine();

                int a = 128;
                int kod;

                kod = 0;
                for (int i = 0; i < ByteInBits; i++)
                {
                    kod += a * message[i];
                    a /= 2;
                }
                code2.WriteByte((byte)kod);

                kod = 0;
                for (int i = 0; i < ByteInBits; i++)
                {
                    kod += a * tableControl[i];
                    a /= 2;
                }
                code2.WriteByte((byte)kod);
            }
        }
    }

    public void Checking(int[,] matrixH, string encodedFileName, string decodedFileName)
    {
        using (StreamReader encoded = new StreamReader(encodedFileName, Encoding.UTF8))
        using (StreamWriter decoded = new StreamWriter(decodedFileName, false, Encoding.UTF8))
        {
            int[] encodedArray = new int[ByteInBits * 2];
            int[] errorArray = new int[ByteInBits];
            int counter = 0;
            int errorCounter = 0;

            while (true)
            {
                int symbol = encoded.Read();
                if (symbol == -1)
                    break;

                if (symbol == 13)  // Sprawdzanie dla znaku CR
                {
                    continue;  // Ignoruj znak CR i przejdź do następnego symbolu
                }
    
                if (symbol != 10)  // Sprawdzanie dla znaku LF
                {
                    encodedArray[counter] = symbol - 48;
                    counter++;
                }
                else
                {
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