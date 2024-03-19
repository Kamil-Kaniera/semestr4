package view;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainViewController {
    private Stage stage;
    @FXML
    private TextField firstKey;
    @FXML
    private TextField secondKey;
    @FXML
    private TextField thirdKey;


    @FXML
    private void initialize() {
    }

    @FXML
    private void onKeyGeneratorButtonClick() {
        firstKey.setText("0123456789ABCDEF");
        secondKey.setText("1133557799BBDDFF");
        thirdKey.setText("0022446688AACCEE");
    }

    @FXML
    private void onLoadButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.showOpenDialog(stage);
    }

    @FXML
    private void onSaveButtonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.showSaveDialog(stage);
    }


}
