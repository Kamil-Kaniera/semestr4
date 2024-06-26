﻿using System;


namespace zad1
{
    class Program
{
    static void Main(string[] args)
    {
        while (true)
        {
            Console.WriteLine("Wybierz operację:");
            Console.WriteLine("1. Kodowanie");
            Console.WriteLine("2. Dekodowanie");
            Console.WriteLine("3. Pokaż autorów");
            Console.WriteLine("4. Wyjście");
            Console.Write("Twój wybór: ");

            string choice = Console.ReadLine();

            switch (choice)
            {
                case "1":
                    EncodeText();
                    break;
                case "2":
                    DecodeText();
                    break;
                case "3":
                    ShowAuthors();
                    break;
                case "4":
                    Environment.Exit(0);
                    break;
                default:
                    Console.WriteLine("Nieprawidłowy wybór. Spróbuj ponownie.");
                    break;
            }
            Console.WriteLine("");
        }
    }

    static void EncodeText()
    {
        int[,] matrixH = {
            {0, 1, 1, 1, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 1, 1, 0, 0,  0, 1, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 1, 0, 1, 0,  0, 0, 1, 0, 0, 0, 0, 0},
            {1, 1, 0, 1, 0, 1, 1, 0,  0, 0, 0, 1, 0, 0, 0, 0},
            {1, 1, 1, 0, 1, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0},
            {1, 1, 1, 1, 0, 1, 0, 1,  0, 0, 0, 0, 0, 1, 0, 0},
            {1, 1, 1, 1, 1, 0, 1, 1,  0, 0, 0, 0, 0, 0, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 1}
        };

        ErrorCorrectingCode errorCorrectingCodeObj = new ErrorCorrectingCode();
        string fileName = "../../data.txt";

        errorCorrectingCodeObj.Coding(matrixH, fileName, "coded.txt", "codedString.txt");
        Console.WriteLine("Plik zakodowany pomyślnie.");
    }
    

    static void DecodeText()
    {
        int[,] matrixH = {
            {0, 1, 1, 1, 0, 0, 0, 0,  1, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 1, 1, 0, 0,  0, 1, 0, 0, 0, 0, 0, 0},
            {1, 0, 0, 0, 1, 0, 1, 0,  0, 0, 1, 0, 0, 0, 0, 0},
            {1, 1, 0, 1, 0, 1, 1, 0,  0, 0, 0, 1, 0, 0, 0, 0},
            {1, 1, 1, 0, 1, 0, 0, 1,  0, 0, 0, 0, 1, 0, 0, 0},
            {1, 1, 1, 1, 0, 1, 0, 1,  0, 0, 0, 0, 0, 1, 0, 0},
            {1, 1, 1, 1, 1, 0, 1, 1,  0, 0, 0, 0, 0, 0, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1,  0, 0, 0, 0, 0, 0, 0, 1}
        };

        ErrorCorrectingCode errorCorrectingCodeObj = new ErrorCorrectingCode();

        errorCorrectingCodeObj.Checking(matrixH, "codedString.txt", "decoded.txt");
        Console.WriteLine("Plik odkodowany pomyślnie.");
    }

    static void ShowAuthors()
    {
        Console.WriteLine("Autorzy programu:");
        Console.WriteLine("Kamil Kaniera 247689");
        Console.WriteLine("Krzysztof Purgat 247771");
    }
}
}