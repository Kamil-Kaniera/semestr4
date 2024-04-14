package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import source.TripleDES;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static view.UtilityFunctions.*;

public class MainViewController {
    private Stage stage;
    @FXML
    private TextField firstKey;
    @FXML
    private TextField secondKey;
    @FXML
    private TextField thirdKey;
    @FXML
    private TextArea plainText;
    @FXML
    private TextArea cryptogram;
    @FXML
    private Label message;

    private String chosenPathPlainText = "";
    private String chosenPathCryptogram = "";


    //Status aktualnie zaznaczonego radio button
    private STATUS toggleStatus = STATUS.WINDOW;

    private enum STATUS {
        WINDOW,
        FILE
    }

    @FXML
    private void initialize() {
        message.setText("");
    }

    @FXML
    private void onCleanButtonClick() {
        message.setText("");
        cryptogram.clear();
        plainText.clear();
    }

    @FXML
    private void onKeyGeneratorButtonClick() {
        firstKey.setText("12345678");
        secondKey.setText("ABCDEF12");
        thirdKey.setText("1A2B3C4D");
    }

    @FXML
    private void onLoadPlainTextButtonClick() {
        message.setText("");
        chosenPathPlainText = LoadFile(plainText, stage);
    }

    @FXML
    private void onLoadCryptogramButtonClick() {
        message.setText("");
        chosenPathCryptogram = LoadFile(cryptogram, stage);
    }


    @FXML
    private void onRadioButtonChange() {
        message.setText("");
        if(toggleStatus == STATUS.WINDOW) toggleStatus = STATUS.FILE;
        else toggleStatus = STATUS.WINDOW;
    }

    @FXML
    private void onEncryptButtonClick(){
        message.setText("");
        byte[] key1 = hexStringToByteArray(firstKey.getText());
        byte[] key2 = hexStringToByteArray(secondKey.getText());
        byte[] key3 = hexStringToByteArray(thirdKey.getText());

        if (toggleStatus == STATUS.WINDOW) encryptWindow(key1, key2, key3);
        else if (toggleStatus == STATUS.FILE) encryptFile(key1, key2, key3, getFileExtension(chosenPathPlainText));
    }

    @FXML
    private void onDecryptButtonClick() {
        message.setText("");
        byte[] key1 = hexStringToByteArray(firstKey.getText());
        byte[] key2 = hexStringToByteArray(secondKey.getText());
        byte[] key3 = hexStringToByteArray(thirdKey.getText());


        if (toggleStatus == STATUS.WINDOW) decryptWindow(key1, key2, key3);
        else if (toggleStatus == STATUS.FILE) decryptFile(key1, key2, key3, getFileExtension(chosenPathCryptogram));
    }

//-------------------------------------------------WINDOW-------------------------------------------------
    private void encryptWindow(byte[] key1,  byte[] key2,  byte[] key3) {
        cryptogram.setText("Szyfrowanie...");

        byte[] message =  plainText.getText().getBytes(StandardCharsets.ISO_8859_1);

        TripleDES tripleDes = new TripleDES();

        byte[] encrypted = tripleDes.tripleSzyfruj(message, key1, key2, key3);

        String encryptedText = byteArrayToHexString(encrypted);

        cryptogram.setText(encryptedText);
    }

    private void decryptWindow(byte[] key1,  byte[] key2,  byte[] key3) {
        plainText.setText("Odszyfrowanie...");

        byte[] encrypted = hexStringToByteArray(cryptogram.getText());

        TripleDES tripleDes = new TripleDES();

        byte[] decrypted = tripleDes.tripleDeszyfruj(encrypted, key1, key2, key3);

        plainText.setText(new String(decrypted, StandardCharsets.ISO_8859_1).replace("\0", ""));
    }


//-------------------------------------------------File-------------------------------------------------
    private void encryptFile(byte[] key1,  byte[] key2,  byte[] key3, String fileExtension) {
        TripleDES tripleDes = new TripleDES();

        try {
            tripleDes.tripleSzyfrujPlik(chosenPathPlainText, "encrypted_file." + fileExtension, key1, key2, key3, true);
            message.setText("Plik pomyślnie zapisany");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decryptFile(byte[] key1,  byte[] key2,  byte[] key3, String fileExtension) {
        TripleDES tripleDes = new TripleDES();

        try {
            tripleDes.tripleSzyfrujPlik(chosenPathCryptogram, "decrypted_file." + fileExtension, key1, key2, key3, false);
            message.setText("Plik pomyślnie zapisany");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
