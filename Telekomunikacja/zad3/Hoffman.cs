using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Runtime.Serialization.Formatters.Binary;
using System.Text;

namespace zad3
{
    [Serializable]
    class HuffmanData
    {
        public HuffmanNode TreeRoot { get; set; }
        public string EncodedText { get; set; }
    }

    [Serializable]
    class HuffmanNode
    {
        public char Symbol { get; set; }
        public int Frequency { get; set; }
        public HuffmanNode Left { get; set; }
        public HuffmanNode Right { get; set; }
    }

    class Huffman
    {
        private static HuffmanNode root;

        public static HuffmanData Encode(string text)
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

            if (nodes.Count == 1)
            {
                root = nodes[0];
            }
            else
            {
                throw new InvalidOperationException("Invalid Huffman tree.");
            }

            Dictionary<char, string> codeDictionary = new Dictionary<char, string>();
            Traverse(root, "", codeDictionary);
            string encodedText = string.Concat(text.Select(c => codeDictionary[c]));

            return new HuffmanData { TreeRoot = root, EncodedText = encodedText };
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

        public static string Decode(HuffmanData data)
        {
            HuffmanNode current = data.TreeRoot;
            string decodedText = "";

            foreach (char bit in data.EncodedText)
            {
                if (bit == '0')
                    current = current.Left;
                else if (bit == '1')
                    current = current.Right;

                if (current.Left == null && current.Right == null)
                {
                    decodedText += current.Symbol;
                    current = data.TreeRoot;
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

                HuffmanData huffmanData = Huffman.Encode(text);
                Console.WriteLine("Tekst po zakodowaniu: " + huffmanData.EncodedText);

                // Wysyłanie danych przez gniazda sieciowe
                SendDataOverSocket(huffmanData);
            }
            catch (Exception ex)
            {
                Console.WriteLine("Błąd: " + ex.Message);
            }
        }

        static void SendDataOverSocket(HuffmanData data)
        {
            try
            {
                IPAddress ipAddress = IPAddress.Parse("192.168.0.2");
                IPEndPoint remoteEP = new IPEndPoint(ipAddress, 11000);

                using (Socket sender = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp))
                {
                    sender.Connect(remoteEP);

                    using (NetworkStream networkStream = new NetworkStream(sender))
                    {
                        BinaryFormatter formatter = new BinaryFormatter();
                        formatter.Serialize(networkStream, data);
                    }

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

                // Odczytanie danych
                using (TcpClient client = listener.AcceptTcpClient())
                using (NetworkStream stream = client.GetStream())
                {
                    BinaryFormatter formatter = new BinaryFormatter();
                    HuffmanData receivedData = (HuffmanData)formatter.Deserialize(stream);

                    string decodedText = Huffman.Decode(receivedData);
                    Console.WriteLine("Odkodowany tekst: " + decodedText);

                    Console.WriteLine("Podaj nazwę pliku do zapisania odkodowanego tekstu:");
                    string fileName = Console.ReadLine();

                    File.WriteAllText("../../" + fileName, decodedText);

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
