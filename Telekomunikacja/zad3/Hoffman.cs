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
        
        public int OriginalTextLength { get; set; }

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
        private static HuffmanNode _root;

        // Metoda kodująca tekst przy użyciu algorytmu Huffmana
        public static HuffmanData Encode(string text)
        {
            // Obliczenie częstotliwości wystąpień poszczególnych znaków
            Dictionary<char, int> frequencies = text
                .GroupBy(c => c)
                .OrderByDescending(g => g.Count())
                .ToDictionary(g => g.Key, g => g.Count());

            // Jeśli tekst zawiera tylko jeden rodzaj znaku, zapisujemy go jako "0" w słowniku kodowania
            if (frequencies.Count == 1)
            {
                char singleChar = frequencies.Keys.First();
                Dictionary<char, string> codeDictionary1 = new Dictionary<char, string> { { singleChar, "0" } };

                // Konwersja tekstu na tablicę bajtów
                byte[] encodedBytes1 = new byte[] { (byte)singleChar };
                return new HuffmanData { EncodedText = encodedBytes1, OriginalTextLength = text.Length };
            }
            
            // Utworzenie węzłów dla każdego znaku
            List<HuffmanNode> nodes = frequencies.Select(kv => new HuffmanNode { Symbol = kv.Key, Frequency = kv.Value }).ToList();

            // Budowa drzewa Huffmana
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
                _root = nodes[0];
            }
            else
            {
                throw new InvalidOperationException("Invalid Huffman tree.");
            }

            // Tworzenie słownika kodowania
            Dictionary<char, string> codeDictionary = new Dictionary<char, string>();
            Traverse(_root, "", codeDictionary);
            string encodedText = string.Concat(text.Select(c => codeDictionary[c]));

            Console.WriteLine("Słownik kodów Huffmana:");
            foreach (var entry in codeDictionary)
            {
                Console.WriteLine($"Symbol: {entry.Key}, Kod: {entry.Value}");
            }
            
            // Wypisanie zakodowanego tekstu
            Console.WriteLine("Zakodowany tekst: " + encodedText);
            
            // Zapisanie oryginalnej długości tekstu
            int originalTextLength = text.Length;
          
            // Konwersja zakodowanego tekstu na bajty
            byte[] encodedBytes = new byte[(encodedText.Length + 7) / 8];
            for (int i = 0; i < encodedText.Length; i += 8)
            {
                // Te linie kodu są odpowiedzialne za konwersję zakodowanego tekstu na tablicę bajtów. 
                // Pierwsza linia pobiera fragment zakodowanego tekstu o długości 8 lub krótszy, a następnie uzupełnia go zerami do pełnego bajtu, jeśli jest krótszy niż 8 bitów. 
                // Druga linia konwertuje ten ciąg znaków reprezentujący binarną postać bajta na rzeczywisty bajt (używając metody Convert.ToByte) i zapisuje go w odpowiednim miejscu w tablicy bajtów encodedBytes.
                // Wartość i / 8 służy do określenia indeksu bajtu w tablicy, który powinien zostać zapisany.
                string byteString = encodedText.Substring(i, Math.Min(8, encodedText.Length - i)).PadRight(8, '0');
                encodedBytes[i / 8] = Convert.ToByte(byteString, 2);
            }

            // Konwersja drzewa Huffmana na bajty
            MemoryStream treeStream = new MemoryStream();
            BinaryFormatter formatter = new BinaryFormatter();
            formatter.Serialize(treeStream, _root);
            byte[] treeBytes = treeStream.ToArray();

            return new HuffmanData { EncodedText = encodedBytes, TreeData = treeBytes, OriginalTextLength = originalTextLength };
        }

        // Metoda rekurencyjna do przejścia drzewa Huffmana i zbudowania słownika kodowania
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

        // Metoda dekodująca zakodowany tekst
        public static string Decode(byte[] encodedBytes, byte[] treeBytes, int originalTextLength)
        {
            // Deserializacja drzewa Huffmana
            BinaryFormatter formatter = new BinaryFormatter();      // Tworzenie obiektu BinaryFormatter, który będzie używany do deserializacji danych.
            MemoryStream treeStream = new MemoryStream(treeBytes);  // Tworzenie strumienia pamięci (MemoryStream) na podstawie danych drzewa przekazanych jako tablica bajtów (treeBytes).
            _root = (HuffmanNode)formatter.Deserialize(treeStream);  // Deserializacja danych drzewa z wcześniej utworzonego strumienia, a następnie przypisanie wyniku deserializacji do zmiennej root.



            // Dekodowanie tekstu
            StringBuilder decodedText = new StringBuilder();
            if (_root != null)
            {
                // Inicjalizacja zmiennej current jako korzeń drzewa Huffmana.
                HuffmanNode current = _root;

                // Iteracja przez tablicę bajtów zawierającą zakodowany tekst.
                foreach (byte b in encodedBytes)
                {
                    // Iteracja przez każdy bit w bajcie (od lewej do prawej).
                    for (int i = 7; i >= 0 && originalTextLength > 0; i--)
                    {
                        // Odczytanie i-tego bitu z bajtu.
                        int bit = (b >> i) & 1;

                        // Przechodzenie w lewo lub prawo w drzewie w zależności od odczytanego bitu.
                        if (bit == 0)
                        {
                            // Przechodzenie w lewo, jeśli bit jest równy 0.
                            if (current != null) current = current.Left;
                        }
                        else if (bit == 1)
                        {
                            // Przechodzenie w prawo, jeśli bit jest równy 1.
                            if (current != null) current = current.Right;
                        }

                        // Sprawdzenie, czy aktualny węzeł jest liściem i dodanie jego symbolu do odkodowanego tekstu.
                        if (current != null && current.Left == null && current.Right == null)
                        {
                            decodedText.Append(current.Symbol);
                            // Po dodaniu symbolu, przechodzimy z powrotem do korzenia, aby kontynuować dekodowanie.
                            current = _root;
                            originalTextLength--;
                        }
                    }
                }

            }
            else
            {
                // Jeśli drzewo jest puste, to zwróć pusty ciąg znaków
                decodedText.Append("");
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
                string text = File.ReadAllText("../../" + fileName, Encoding.UTF8);
                Console.WriteLine("Tekst przed kodowaniem: " + text);

                HuffmanData huffmanData = Huffman.Encode(text);

                // Zapisanie zakodowanego tekstu i drzewa do pliku
                using (FileStream fileStream = new FileStream("../../compressed.bin", FileMode.Create))
                using (BinaryWriter writer = new BinaryWriter(fileStream))
                {
                    writer.Write(huffmanData.OriginalTextLength); // Zapisz długość oryginalnego tekstu
                    writer.Write(huffmanData.EncodedText.Length); // Zapisz długość zakodowanego tekstu
                    writer.Write(huffmanData.EncodedText); // Zapisz zakodowany tekst
                    if (huffmanData.EncodedText.Length == 1)
                    {
                        
                    }
                    else
                    {
                        writer.Write(huffmanData.TreeData.Length); // Zapisz długość danych drzewa
                        writer.Write(huffmanData.TreeData); // Zapisz dane drzewa
                    }
                    
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
                if (ipAddressString != null)
                {
                    IPAddress ipAddress = IPAddress.Parse(ipAddressString);
                    IPEndPoint remoteEp = new IPEndPoint(ipAddress, 11000);

                    using (Socket sender = new Socket(ipAddress.AddressFamily, SocketType.Stream, ProtocolType.Tcp))
                    {
                        sender.Connect(remoteEp);

                        using (NetworkStream networkStream = new NetworkStream(sender))
                        {
                            networkStream.Write(compressedFileData, 0, compressedFileData.Length);
                        }

                        Console.WriteLine("Skompresowany plik wysłany pomyślnie.");
                    }
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
                    int originalTextLength = reader.ReadInt32(); // Odczytaj długość oryginalnego tekstu
                    int encodedTextLength = reader.ReadInt32(); // Odczytaj długość zakodowanego tekstu
                    byte[] encodedText = reader.ReadBytes(encodedTextLength); // Odczytaj zakodowany tekst
                    
                    // Dekompresja pliku (jeśli jest 1 rodzaj znaku)
                    if (encodedText.Length == 1)
                    {
                        string decodedText = new string((char)encodedText[0], originalTextLength);
                        Console.WriteLine("Odkodowany tekst: " + decodedText);

                        Console.WriteLine("Podaj nazwę pliku do zapisania odkodowanego tekstu:");
                        string fileName = Console.ReadLine();

                        File.WriteAllText("../../" + fileName, decodedText);

                        Console.WriteLine("Plik zapisany pomyślnie.");
                    }
                    else
                    {
                        int treeDataLength = reader.ReadInt32(); // Odczytaj długość danych drzewa
                        byte[] treeData = reader.ReadBytes(treeDataLength); // Odczytaj dane drzewa
                        
                        string decodedText = Huffman.Decode(encodedText, treeData, originalTextLength);
                        Console.WriteLine("Odkodowany tekst: " + decodedText);

                        Console.WriteLine("Podaj nazwę pliku do zapisania odkodowanego tekstu:");
                        string fileName = Console.ReadLine();

                        File.WriteAllText("../../" + fileName, decodedText);

                        Console.WriteLine("Plik zapisany pomyślnie.");
                    } 
                   
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
