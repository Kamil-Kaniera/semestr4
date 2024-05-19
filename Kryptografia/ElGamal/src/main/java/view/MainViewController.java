package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import source.ElGamal;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static view.UtilityFunctions.*;

public class MainViewController {
    private Stage stage;
    @FXML
    private TextField publicKeyG;
    @FXML
    private TextField publicKeyH;
    @FXML
    private TextField privateKeyA;
    @FXML
    private TextField modN;
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

    private ElGamal elGamal;

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
        elGamal = new ElGamal(2048);
        publicKeyG.setText(elGamal.getG().toString(16));
        publicKeyH.setText(elGamal.getH().toString(16));
        privateKeyA.setText(elGamal.getA().toString(16));
        modN.setText(elGamal.getP().toString(16));
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
        byte[] g = elGamal.getG().toByteArray();
        byte[] h = elGamal.getH().toByteArray();
        byte[] a = elGamal.getA().toByteArray();
        byte[] p = elGamal.getP().toByteArray();

        if (toggleStatus == STATUS.WINDOW) encryptWindow(p, g, h, a);
        else if (toggleStatus == STATUS.FILE) encryptFile(p, g, h, a, getFileExtension(chosenPathPlainText));
    }

    @FXML
    private void onDecryptButtonClick() {
        message.setText("");
        byte[] g = elGamal.getG().toByteArray();
        byte[] h = elGamal.getH().toByteArray();
        byte[] a = elGamal.getA().toByteArray();
        byte[] p = elGamal.getP().toByteArray();



        if (toggleStatus == STATUS.WINDOW) decryptWindow(p, g, h, a);
        else if (toggleStatus == STATUS.FILE) decryptFile(p, g, h, a, getFileExtension(chosenPathCryptogram));
    }

//-------------------------------------------------WINDOW-------------------------------------------------
    private void encryptWindow(byte[] p,  byte[] g,  byte[] h,  byte[] a) {
        cryptogram.setText("Szyfrowanie...");

        byte[] message =  plainText.getText().getBytes(StandardCharsets.ISO_8859_1);

        ElGamal elGamal = new ElGamal(p, g, h, a);

        byte[] encrypted = elGamal.encryptBytes(message);

        String encryptedText = byteArrayToHexString(encrypted);

        cryptogram.setText(encryptedText);
    }

    private void decryptWindow(byte[] p,  byte[] g,  byte[] h, byte[] a) {
        plainText.setText("Odszyfrowanie...");

        byte[] encrypted = hexStringToByteArray(cryptogram.getText());

        ElGamal elGamal = new ElGamal(p, g, h, a);

        byte[] decrypted = elGamal.decryptBytes(encrypted);

        plainText.setText(new String(decrypted, StandardCharsets.ISO_8859_1).replace("\0", ""));
    }


//-------------------------------------------------File-------------------------------------------------
    private void encryptFile(byte[] p,  byte[] g,  byte[] h,  byte[] a,  String fileExtension) {
        ElGamal elGamal = new ElGamal(p, g, h, a);

        try {
            elGamal.encryptFile(chosenPathPlainText, "encrypted_file." + fileExtension);
            message.setText("Plik pomyślnie zapisany");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decryptFile(byte[] p,  byte[] g,  byte[] h, byte[] a,  String fileExtension) {
        ElGamal elGamal = new ElGamal(p, g, h, a);

        try {
            elGamal.decryptFile(chosenPathCryptogram, "decrypted_file." + fileExtension);
            message.setText("Plik pomyślnie zapisany");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
