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
        public byte[] EncodedText { get; set; }
        public byte[] TreeData { get; set; }
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

            // Convert encoded text to bytes
            byte[] encodedBytes = new byte[(encodedText.Length + 7) / 8];
            for (int i = 0; i < encodedText.Length; i += 8)
            {
                string byteString = encodedText.Substring(i, Math.Min(8, encodedText.Length - i)).PadRight(8, '0');
                encodedBytes[i / 8] = Convert.ToByte(byteString, 2);
            }

            // Convert Huffman tree to bytes
            MemoryStream treeStream = new MemoryStream();
            BinaryFormatter formatter = new BinaryFormatter();
            formatter.Serialize(treeStream, root);
            byte[] treeBytes = treeStream.ToArray();

            return new HuffmanData { EncodedText = encodedBytes, TreeData = treeBytes };
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

        public static string Decode(byte[] encodedBytes, byte[] treeBytes)
        {
            // Deserialize Huffman tree
            BinaryFormatter formatter = new BinaryFormatter();
            MemoryStream treeStream = new MemoryStream(treeBytes);
            root = (HuffmanNode)formatter.Deserialize(treeStream);

            // Decode text
            StringBuilder decodedText = new StringBuilder();
            HuffmanNode current = root;
            foreach (byte b in encodedBytes)
            {
                for (int i = 7; i >= 0; i--)
                {
                    int bit = (b >> i) & 1;
                    if (bit == 0)
                        current = current.Left;
                    else if (bit == 1)
                        current = current.Right;

                    if (current.Left == null && current.Right == null)
                    {
                        decodedText.Append(current.Symbol);
                        current = root;
                    }
                }
            }

            return decodedText.ToString();
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
                Console.WriteLine("2. Przesłanie skompresowanego pliku");
                Console.WriteLine("3. Dekodowanie");
                Console.WriteLine("4. Pokaż autorów");
                Console.WriteLine("5. Wyjście");
                Console.Write("Twój wybór: ");

                string choice = Console.ReadLine();

                switch (choice)
                {
                    case "1":
                        EncodeText();
                        break;
                    case "2":
                        SendCompressedFile();
                        break;
                    case "3":
                        DecodeText();
                        break;
                    case "4":
                        ShowAuthors();
                        break;
                    case "5":
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

                // Zapisanie zakodowanego tekstu i drzewa do pliku
                using (FileStream fileStream = new FileStream("../../compressed.bin", FileMode.Create))
                using (BinaryWriter writer = new BinaryWriter(fileStream))
                {
                    writer.Write(huffmanData.EncodedText.Length); // Zapisz długość zakodowanego tekstu
                    writer.Write(huffmanData.EncodedText); // Zapisz zakodowany tekst
                    writer.Write(huffmanData.TreeData.Length); // Zapisz długość danych drzewa
                    writer.Write(huffmanData.TreeData); // Zapisz dane drzewa
                }

                Console.WriteLine("Dane zakodowane i zapisane do pliku 'compressed.bin'.");
            }
            catch (Exception ex)
            {
                Console.WriteLine("Błąd: " + ex.Message);
            }
        }

        static void SendCompressedFile()
        {
            Console.WriteLine("Podaj adres IP docelowego komputera:");
            string ipAddressString = Console.ReadLine();

            try
            {
                // Odczytanie zakodowanego pliku
                byte[] compressedFileData = File.ReadAllBytes("../../compressed.bin");

                // Utworzenie połączenia z docelowym komputerem i przesłanie pliku
                IPAddress ipAddress = IPAddress.Parse(ipAddressString);
                IPEndPoint remoteEP = new IPEndPoint(ipAddress, 11000);

                using (Socket sender = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp))
                {
                    sender.Connect(remoteEP);

                    using (NetworkStream networkStream = new NetworkStream(sender))
                    {
                        networkStream.Write(compressedFileData, 0, compressedFileData.Length);
                    }

                    Console.WriteLine("Skompresowany plik wysłany pomyślnie.");
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("Błąd podczas przesyłania pliku: " + ex.Message);
            }
        }

        static void DecodeText()
        {
            try
            {
                // Odbiór skompresowanego pliku
                TcpListener listener = new TcpListener(IPAddress.Any, 11000);
                listener.Start();

                Console.WriteLine("Oczekiwanie na połączenie...");

                using (TcpClient client = listener.AcceptTcpClient())
                using (NetworkStream stream = client.GetStream())
                using (BinaryReader reader = new BinaryReader(stream))
                {
                    int encodedTextLength = reader.ReadInt32(); // Odczytaj długość zakodowanego tekstu
                    byte[] encodedText = reader.ReadBytes(encodedTextLength); // Odczytaj zakodowany tekst
                    int treeDataLength = reader.ReadInt32(); // Odczytaj długość danych drzewa
                    byte[] treeData = reader.ReadBytes(treeDataLength); // Odczytaj dane drzewa

                    // Dekompresja pliku
                    string decodedText = Huffman.Decode(encodedText, treeData);
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

        static void ShowAuthors()
        {
            Console.WriteLine("Autorzy programu:");
            Console.WriteLine("Kamil Kaniera 247689");
            Console.WriteLine("Krzysztof Purgat 247771");
        }
    }
}
