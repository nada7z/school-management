package schoolmanagement.smproject;

import org.springframework.stereotype.Component;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

@Component
public class MainController {

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        messageLabel.setText("✅ School Management System is running!");
    }
}