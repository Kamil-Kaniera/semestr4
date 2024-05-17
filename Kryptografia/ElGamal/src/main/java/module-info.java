module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;


    exports view;
    opens view to javafx.fxml;
}