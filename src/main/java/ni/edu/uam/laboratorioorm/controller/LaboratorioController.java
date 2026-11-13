package ni.edu.uam.laboratorioorm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class LaboratorioController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
