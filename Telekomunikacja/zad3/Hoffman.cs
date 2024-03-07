using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace zad3
{
    class HuffmanNode
{
    public char Symbol { get; set; }
    public int Frequency { get; set; }
    public HuffmanNode Left { get; set; }
    public HuffmanNode Right { get; set; }
}

class Huffman
{
    // Zmienna przechowująca referencję do korzenia drzewa Huffmana
    private static HuffmanNode root;

    public static Dictionary<char, string> BuildCodeDictionary(string text)
    {
        Dictionary<char, int> frequencies = text
            .GroupBy(c => c)
            .OrderByDescending(g => g.Count())
            .ToDictionary(g => g.Key, g => g.Count());
        
        List<HuffmanNode> nodes = frequencies.Select(kv => new HuffmanNode { Symbol = kv.Key, Frequency = kv.Value }).ToList();

        while (nodes.Count > 1)
        {
            nodes = nodes.OrderBy(n => n.Frequency).ToList();

            HuffmanNode parent = new HuffmanNode
            {
                Left = nodes[0],
                Right = nodes[1],
                Frequency = nodes[0].Frequency + nodes[1].Frequency
            };

            nodes.RemoveRange(0, 2);
            nodes.Add(parent);
        }

        // Sprawdzamy, czy istnieje więcej niż jeden węzeł w liście
        if (nodes.Count == 1)
        {
            root = nodes[0];
            Dictionary<char, string> codeDictionary = new Dictionary<char, string>();
            Traverse(root, "", codeDictionary);
            return codeDictionary;
        }
        else
        {
            throw new InvalidOperationException("Nieprawidłowe drzewo Huffmana.");
        }
    }

    private static void Traverse(HuffmanNode node, string code, Dictionary<char, string> codeDictionary)
    {
        if (node.Left == null && node.Right == null)
        {
            codeDictionary.Add(node.Symbol, code);
            return;
        }

        Traverse(node.Left, code + "0", codeDictionary);
        Traverse(node.Right, code + "1", codeDictionary);
    }

    public static string Encode(string text, Dictionary<char, string> codeDictionary)
    {
        foreach (var kvp in codeDictionary)
        {
            Console.WriteLine("Klucz: " + kvp.Key + ", Wartość: " + kvp.Value);
        }        return string.Concat(text.Select(c => codeDictionary[c]));
    }

    public static string Decode(string encodedText)
    {
        HuffmanNode current = root;
        string decodedText = "";

        foreach (char bit in encodedText)
        {
            if (bit == '0')
                current = current.Left;
            else if (bit == '1')
                current = current.Right;

            if (current.Left == null && current.Right == null)
            {
                decodedText += current.Symbol;
                current = root;
            }
        }

        return decodedText;
    }
}

class Program
{
    static void Main(string[] args)
    {
        while (true)
        {
            Console.WriteLine("Wybierz operację:");
            Console.WriteLine("1. Kodowanie");
            Console.WriteLine("2. Dekodowanie");
            Console.WriteLine("3. Wyjście");
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
                    Environment.Exit(0);
                    break;
                default:
                    Console.WriteLine("Nieprawidłowy wybór. Spróbuj ponownie.");
                    break;
            }
        }
    }

    static void EncodeText()
    {
        Console.WriteLine("Podaj nazwę pliku do zakodowania:");
        string fileName = Console.ReadLine();

        try
        {
            string text = File.ReadAllText("../../" + fileName);
            Console.WriteLine("Tekst przed kodowaniem: " + text);

            Dictionary<char, string> codeDictionary = Huffman.BuildCodeDictionary(text);
            string encodedText = Huffman.Encode(text, codeDictionary);
            Console.WriteLine("Tekst po zakodowaniu: " + encodedText);

            // Wysyłanie zakodowanego tekstu przez gniazda sieciowe
            SendDataOverSocket(encodedText);
        }
        catch (Exception ex)
        {
            Console.WriteLine("Błąd: " + ex.Message);
        }
    }

    static void SendDataOverSocket(string data)
    {
        try
        {
            IPAddress ipAddress = IPAddress.Parse("192.168.0.2");
            IPEndPoint remoteEP = new IPEndPoint(ipAddress, 11000);

            using (Socket sender = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp))
            {
                sender.Connect(remoteEP);

                byte[] byteData = Encoding.ASCII.GetBytes(data);
                sender.Send(byteData);

                Console.WriteLine("Dane wysłane pomyślnie.");
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine("Błąd podczas wysyłania danych: " + ex.Message);
        }
    }

    static void DecodeText()
    {
        try
        {
            // Nasłuchiwanie na gnieździe
            TcpListener listener = new TcpListener(IPAddress.Any, 11000);
            listener.Start();

            Console.WriteLine("Oczekiwanie na połączenie...");

            using (TcpClient client = listener.AcceptTcpClient())
            using (NetworkStream stream = client.GetStream())
            {
                byte[] buffer = new byte[1024];
                int bytesRead = stream.Read(buffer, 0, buffer.Length);
                string encodedText = Encoding.ASCII.GetString(buffer, 0, bytesRead);

                Console.WriteLine("Otrzymany zakodowany tekst: " + encodedText);

                string decodedText = Huffman.Decode(encodedText);
                Console.WriteLine("Odkodowany tekst: " + decodedText);

                Console.WriteLine("Podaj nazwę pliku do zapisania odkodowanego tekstu:");
                string fileName = Console.ReadLine();

                File.WriteAllText(fileName, decodedText);

                Console.WriteLine("Plik zapisany pomyślnie.");
            }

            listener.Stop();
        }
        catch (Exception ex)
        {
            Console.WriteLine("Błąd: " + ex.Message);
        }
    }
}

}
