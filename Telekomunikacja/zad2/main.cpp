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
#define C 'C'     // Transmission initialization by receiver - sluzy do inicjacji transmisji przez odbiornik
#define SUB 26    // Substitution character - sluzy on jako wypelniacz/dopelnienie w blokach danych

using namespace std;

//################## SYSTEMOWE ZMIENNE GLOBALNE   #############################
HANDLE handleCom;
BOOL isReadyPort;
DCB controlDCB;
COMMTIMEOUTS objCommtime;

int BaudRate = 9600;
bool CRC;             // CRC wybor
char COM[10];         // Nazwa portu szeregowego
char fileToSend[100]; // Nazwa pliku wysyłanego
char fileToSave[100]; // Nazwa pliku do zapisania

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

short int CRC16(char *fileBuffer) {                         // obliczanie sumy kontrolnej crc16
    int tmp = 0, val = 0x18005 << 15;                       // val - 17 bitow
    for (int i = 0; i < 3; i++) {                           //przetwarzanie pierwszych 3 bajtow danych z bufora
        tmp = tmp * 256 + (unsigned char) fileBuffer[i];    //zawartosc bajtow jest dodawana do zmiennej tmp
    }
    tmp *= 256;                                     // przesuniecie o 8 bitow, bajt

    for (int i = 3; i < 134; i++) {                 // przetwarzanie pozostalych bajtow bloku danych
        if (i < 128) {
            tmp += (unsigned char) fileBuffer[i];   // jesli znajduje sie w zakresie danych, jest dodawany do zmiennej tmp
        }
        for (int j = 0; j < 8; j++) {       // przetwarza kazdy bit bajtu
            if (tmp & (1 << 31)) {          // jesli najabrdziej znaczacy bit jest ustawiony,
                tmp ^= val;                 // dochodzi do XORowania
            }
            tmp <<= 1;                      // przesuniecie bitowe o jedno miejsce
        }
    }
    return tmp >> 16;                       // zwraca 16 bitowa sume kontrolna crc dla bloku danych
}

void receiving() {                      //otrzymywanie danych
    char buf[3], fileBuffer[128];

    cout << "Enter the file name to save: ";
    cin.getline(fileToSave, 100);

    initialize(COM);                    //inicjalizacja portu

    buf[0] = CRC ? C : NAK;             //Ustawienie znaku NAK, jeśli używane jest CRC, lub znaku C, jeśli nie jest używane CRC.
    sendCOM(buf, 1);                    //wyslanie odpowiedniego znaku

    FILE *f = fopen(fileToSave, "wb");

    receiveCOM(buf, 1);                 //otrzymanie pierwszego znaku odnosnie crc
    while (true) {
        unsigned short sum, sumc;       // nastepuje odbieranie danych w petli
        receiveCOM(buf + 1, 2);         //odebranie danych
        receiveCOM(fileBuffer, 128);    //odebranie danych

        sum = sumc = 0;
        receiveCOM((char *) &sum, CRC ? 2 : 1);     //odebranie danych

        if (CRC) {
            sumc = CRC16(fileBuffer);
        } else {
            for (int i = 0; i < 128; i++) {
                sumc += (unsigned char) fileBuffer[i];
            }
            sumc %= 256;
        }

        if (sum != sumc) {
            buf[0] = NAK;
            sendCOM(buf, 1);
            continue;
        }

        buf[0] = ACK;
        sendCOM(buf, 1);

        receiveCOM(buf, 1);
        if (buf[0] == EOT) {
            unsigned char last = 127;
            while (fileBuffer[last] == SUB) {
                last--;
            }
            fwrite(fileBuffer, last + 1, 1, f);
            break;
        }
        fwrite(fileBuffer, 128, 1, f);
    }
    fclose(f);
    buf[0] = ACK;
    sendCOM(buf, 1);
}

void sending() {
    char buf[3], fileBuffer[128];

    cout << "Enter the file name to send: ";
    cin.getline(fileToSend, 100);

    initialize(COM);

    receiveCOM(buf, 1);
    if (buf[0] == NAK) {
        CRC = false;
    } else if (buf[0] == C) {
        CRC = true;
    } else {
        return;
    }

    int no = 1;
    FILE *f = fopen(fileToSend, "rb");
    fseek(f, 0, SEEK_END);
    int fsize = ftell(f);
    fseek(f, 0, SEEK_SET);

    while (ftell(f) < fsize) {
        unsigned char length = fread(fileBuffer, 1, 128, f);
        for (int i = length; i < 128; i++) {
            fileBuffer[i] = SUB;
        }
        unsigned short sum = 0;

        sum = 0;

        if (CRC) {
            sum = CRC16(fileBuffer);
        } else {
            for (int i = 0; i < 128; i++)
                sum += (unsigned char) fileBuffer[i];
            sum %= 256;
        }

        buf[0] = SOH;
        buf[1] = no;
        buf[2] = 255 - no;

        sendCOM(buf, 3);
        sendCOM(fileBuffer, 128);
        sendCOM((char *) &sum, CRC ? 2 : 1);

        receiveCOM(buf, 1);
        if (buf[0] == ACK) {
            no++;
        } else {
            fseek(f, -128, SEEK_CUR);
        }
    }

    fclose(f);
    do {
        buf[0] = EOT;
        sendCOM(buf, 1);
        receiveCOM(buf, 1);
    } while (buf[0] != ACK);
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

    int crc_option;
    do {
        cout << "Czy chcesz użyć algorytmu CRC: \n"
                "1. Tak\n"
                "2. Nie\n"
                "Opcja: ";
        isCorrect(crc_option);
    } while (!(crc_option == 1 || crc_option == 2));

    CRC = crc_option == 1;

    switch (wybor) {
        case 1: sending(); break;
        case 2: receiving(); break;
    }

    return 0;
}