package view;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
    private ToggleGroup toggles;

    //Status aktualnie zaznaczonego radio button
    private String toggleStatus;


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

    @FXML
    private void onRadioButtonChange() {
        RadioButton button = (RadioButton) toggles.getSelectedToggle();
        toggleStatus = button.getText();
    }

}
