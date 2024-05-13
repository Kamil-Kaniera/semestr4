//Kamil Kaniera, Krzysztof Purgat
#include <windows.h>
#include <stdio.h>
#include <string.h>
#include <iostream>
#include <limits>

#define SOH 0x01  // Start of Header - oznacza poczatek bloku danych podczas odbierania lub wysylania
#define EOT 0x04  // End of Transmission - sygnalizuje zakonczenie transmisji danych
#define ACK 0x06  // Acknowledgment - potwierdza poprawny odbior blokow danych
#define NAK 0x15  // Negative Acknowledgment
#define C   0x43  // Transmission initialization by receiver - sluzy do inicjacji transmisji przez odbiornik
#define SUB 26    // Substitution character - sluzy on jako wypelniacz/dopelnienie w blokach danych

using namespace std;

//################## SYSTEMOWE ZMIENNE GLOBALNE   #############################
HANDLE handleCom;           // Handle do portu szeregowego
BOOL isReadyPort;           // Flaga informująca o gotowości portu
DCB controlDCB;             // Ustawienia portu szeregowego
COMMTIMEOUTS objCommtime;   // Ustawienia czasowe portu szeregowego

int BaudRate = 9600;        // Szybkość transmisji danych
bool CRC;                    // Wybór CRC (Cyclic Redundancy Check)
char COM[10];                // Nazwa portu szeregowego
char fileToSend[100];        // Nazwa pliku do wysłania
char fileToSave[100];        // Nazwa pliku do zapisania


template<typename Type>                 // funkcja szablonowa do sprawdzenia, czy zostaly podane odpowiednie dane
void isCorrect(Type &variable) {
    while ((!(cin >> variable))) {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        cout << "Zle dane, podaj ponownie: ";
    }
    cin.ignore(numeric_limits<streamsize>::max(), '\n');
}

void initialize(char *chosenPort) { // inicjalizacja portu
    handleCom = CreateFile(chosenPort, GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, NULL);
    isReadyPort = SetupComm(handleCom, 1, 128);    // ustawia rozmiar bufora wejsciowego i wyjsciowego,  1 oznacza liczbę buforów wejściowych, a 128 to maksymalny rozmiar bufora wyjściowego.
    isReadyPort = GetCommState(handleCom, &controlDCB);            // zapisanie ustawien portu szeregowego w controlDCB
    controlDCB.BaudRate = BaudRate;                                             // BaudRate ustawia szybkość transmisji danych
    controlDCB.ByteSize = 8;                        // ustawia rozmiar bajtu
    controlDCB.Parity = NOPARITY;                  //konfiguracja bitu parzystości
    controlDCB.StopBits = ONESTOPBIT;               // ustawia jeden bit stopu
    controlDCB.fAbortOnError = TRUE;
    controlDCB.fOutX = FALSE;                      // XON/XOFF WYLACZANIE DO TRANSMISJI
    controlDCB.fInX = FALSE;                       // XON/XOFF WYLACZANIE DO ODBIERANIA
    controlDCB.fOutxCtsFlow = FALSE;               // WLACZANIE CTS flow control
    controlDCB.fRtsControl = RTS_CONTROL_HANDSHAKE;
    controlDCB.fOutxDsrFlow = FALSE;               //WLACZENIE DSR FLOW CONTROL
    controlDCB.fDtrControl = DTR_CONTROL_ENABLE;
    controlDCB.fDtrControl = DTR_CONTROL_DISABLE;
    controlDCB.fDtrControl = DTR_CONTROL_HANDSHAKE;
    isReadyPort = SetCommState(handleCom, &controlDCB);
}

void receiveCOM(char *sign, int length) {       // odbieranie danych przez port szeregowy
    DWORD pos = 0, num;
    while (pos < length) {
        ReadFile(handleCom, sign + pos, length - pos, &num, NULL);
        pos += num;
    }
}

void sendCOM(char *sign, int length) {          // wysylanie danych przez port szeregowy
    DWORD num;
    WriteFile(handleCom, sign, length, &num, NULL);
}


// Funkcja do obliczania CRC16
short int CRC16(char *fileBuffer) {
    short unsigned int crc = 0xFFFF; // Początkowa wartość CRC
    const unsigned int CRC16_POLY = 0x18005; // 0x8005 przesunięte o 15 miejsc w lewo


    for (int i = 0; i < 128; ++i) {
        crc ^= static_cast<unsigned short >(fileBuffer[i]) << 8; // XOR bajtu danych z CRC

        for (int j = 0; j < 8; ++j) {
            if (crc & 0x8000) {
                crc = (crc << 1) ^ CRC16_POLY; // Jeśli najbardziej znaczący bit jest ustawiony, wykonaj XOR z przesuniętym polinomem CRC16
            } else {
                crc <<= 1;
            }
        }
    }

    return static_cast<short int>(crc);
}

void receiving() { // Funkcja odbierania danych
    char buf[3], fileBuffer[128]; // Bufory do przechowywania danych

    cout << "Podaj nazwe pliku do zapisu: "; // Prośba o wprowadzenie nazwy pliku do zapisu
    cin.getline(fileToSave, 100);

    initialize(COM); // Inicjalizacja portu szeregowego

    buf[0] = CRC ? C : NAK; // Ustawienie znaku NAK, jeśli używane jest CRC, lub znaku C, jeśli nie jest używane CRC.
    sendCOM(buf, 1); // Wysłanie odpowiedniego znaku

    FILE *f = fopen(fileToSave, "wb"); // Otwarcie pliku do zapisu w trybie binarnym

    receiveCOM(buf, 1); // Odbiór pierwszego znaku dotyczącego CRC
    while (true) { // Pętla nieskończona
        unsigned short sum, sumc; // Zmienne przechowujące sumy kontrolne

        receiveCOM(buf + 1, 2); // Odbiór danych nagłówka
        receiveCOM(fileBuffer, 128); // Odbiór danych bloku

        sum = sumc = 0; // Zerowanie sum kontrolnych

        receiveCOM((char *) &sum, CRC ? 2 : 1); // Odbiór sumy kontrolnej

        if (CRC) { // Jeśli używane jest CRC
            sumc = CRC16(fileBuffer); // Obliczenie sumy kontrolnej dla otrzymanego bloku danych
        } else { // Jeśli CRC nie jest używane
            for (int i = 0; i < 128; i++) {
                sumc += (unsigned char) fileBuffer[i]; // Obliczenie sumy kontrolnej dla otrzymanego bloku danych
            }
            sumc %= 256; // Ustalenie wartości modulo 256
        }

        if (sum != sumc) { // Sprawdzenie, czy sumy kontrolne się zgadzają
            buf[0] = NAK; // Jeśli nie, wysłanie NAK
            sendCOM(buf, 1);
            continue;
        }

        buf[0] = ACK; // Wysłanie potwierdzenia (ACK) odbioru bloku danych
        sendCOM(buf, 1);

        receiveCOM(buf, 1); // Odbiór kolejnego znaku

        if (buf[0] == EOT) { // Jeśli otrzymano sygnał EOT (koniec transmisji)
            unsigned char last = 127;
            while (fileBuffer[last] == SUB) { // Znajdź ostatni niepusty bajt w buforze danych
                last--;
            }
            fwrite(fileBuffer, last + 1, 1, f); // Zapisz dane do pliku
            break; // Zakończ pętlę
        }
        fwrite(fileBuffer, 128, 1, f); // Zapisz dane do pliku
    }
    fclose(f); // Zamknięcie pliku
    buf[0] = ACK; // Wysłanie potwierdzenia odbioru całego pliku (ACK)
    sendCOM(buf, 1);
}



void sending() {
    char buf[3], fileBuffer[128]; // Bufory do przechowywania danych do wysłania

    // Prośba użytkownika o wprowadzenie nazwy pliku do wysłania
    cout << "Podaj nazwe pliku do wyslania: ";
    cin.getline(fileToSend, 100);

    // Inicjalizacja portu szeregowego
    initialize(COM);

    // Odbieranie początkowej odpowiedzi od odbiorcy
    receiveCOM(buf, 1);

    // Określenie, czy należy używać CRC na podstawie otrzymanej odpowiedzi
    if (buf[0] == NAK) {
        CRC = false;
    } else if (buf[0] == C) {
        CRC = true;
    } else {
        // Jeśli otrzymana odpowiedź nie jest ani NAK, ani C, zakończ funkcję
        return;
    }

    int no = 1; // Inicjalizacja numeru bloku
    FILE *f = fopen(fileToSend, "rb"); // Otwarcie pliku do odczytu
    fseek(f, 0, SEEK_END); // Przesunięcie wskaźnika pliku na koniec pliku w celu określenia jego rozmiaru
    int fsize = ftell(f); // Pobranie rozmiaru pliku
    fseek(f, 0, SEEK_SET); // Przesunięcie wskaźnika pliku z powrotem na początek pliku

    // Pętla, dopóki cały plik nie zostanie odczytany i wysłany
    while (ftell(f) < fsize) {
        // Odczyt danych z pliku do bufora pliku
        unsigned char length = fread(fileBuffer, 1, 128, f);

        // Uzupełnienie pozostałej przestrzeni w buforze znakami SUB
        for (int i = length; i < 128; i++) {
            fileBuffer[i] = SUB;
        }

        unsigned short sum = 0; // Inicjalizacja sumy kontrolnej

        // Obliczenie sumy kontrolnej w zależności od tego, czy CRC jest włączone
        if (CRC) {
            sum = CRC16(fileBuffer);
        } else {
            for (int i = 0; i < 128; i++) {
                sum += (unsigned char) fileBuffer[i];
            }
            sum %= 256;
        }

        // Przygotowanie nagłówka dla bloku danych
        buf[0] = SOH;
        buf[1] = no;
        buf[2] = 255 - no;

        // Wysłanie nagłówka, bloku danych i sumy kontrolnej
        sendCOM(buf, 3);
        sendCOM(fileBuffer, 128);
        sendCOM((char *) &sum, CRC ? 2 : 1);

        // Odbiór potwierdzenia od odbiorcy
        receiveCOM(buf, 1);

        // Jeśli otrzymano potwierdzenie, zwiększ numer bloku
        if (buf[0] == ACK) {
            no++;
        } else {
            // Jeśli potwierdzenie nie zostało otrzymane, przewiń wskaźnik pliku wstecz
            fseek(f, -128, SEEK_CUR);
        }
    }

    // Zamknięcie pliku
    fclose(f);

    // Wysłanie sygnału końca transmisji (EOT)
    do {
        buf[0] = EOT;
        sendCOM(buf, 1);
        receiveCOM(buf, 1);
    } while (buf[0] != ACK); // Powtarzaj, dopóki nie zostanie otrzymane potwierdzenie
}


static void ShowAuthors()
{
    system("CLS");
    cout<<"Autorzy programu:\n";
    cout<<"Kamil Kaniera 247689\n";
    cout<<"Krzysztof Purgat 247771\n";
}

int main() {

    int wybor;

    do {
        cout << "Wybierz odpowiednia funkcjonalnosc: \n"
                "1. Nadwaca\n"
                "2. Odbiorca\n"
                "3. Wyswietl autorow\n"
                "Opcja: ";
        isCorrect(wybor);
        if(wybor == 3) ShowAuthors();
    } while (!(wybor == 1 || wybor == 2));

    int numerPortu;
    do {
        cout << "Wybierz port: \n"
             << "1. COM1\n"
             << "2. COM2\n"
             << "3. COM3\n"
             << "4. COM4\n"
             << "5. COM5\n"
             << "Opcja: ";
        isCorrect(numerPortu);
    } while (!(numerPortu >= 1 && numerPortu <= 5));

    switch (numerPortu) {
        case 1: strcpy(COM, "COM1"); break;
        case 2: strcpy(COM, "COM2"); break;
        case 3: strcpy(COM, "COM3"); break;
        case 4: strcpy(COM, "COM4"); break;
        case 5: strcpy(COM, "COM5"); break;
    }


    switch (wybor) {
        case 1: sending(); break;
        case 2:
            int crc_option;
            do {
                cout << "Czy chcesz uzyc algorytmu CRC: \n"
                        "1. Tak\n"
                        "2. Nie\n"
                        "Opcja: ";
                isCorrect(crc_option);
            } while (!(crc_option == 1 || crc_option == 2));
            CRC = crc_option == 1;
            receiving(); break;
    }

    return 0;
}