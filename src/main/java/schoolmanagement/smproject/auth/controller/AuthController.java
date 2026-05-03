package schoolmanagement.smproject.auth.controller;

import schoolmanagement.smproject.auth.service.AuthService;
import schoolmanagement.smproject.common.SessionManager;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

@Component
public class AuthController {

    private final AuthService authService;
    private Runnable onLoginSuccess;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private VBox formContainer; // The main VBox holding the form

    // Constructor injection (Spring best practice)
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Called automatically after FXML is loaded.
     * Sets up animations and initial state.
     */
    @FXML
    public void initialize() {
        // Clear any previous error
        hideError();

        // Add fade-in animation to the form
        if (formContainer != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(500), formContainer);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        }

        // Allow Enter key to trigger login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleLogin();
            }
        });
    }

    /**
     * Handles the login button click or Enter key press.
     */
    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Basic validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            shakeAnimation(usernameField);
            return;
        }

        // Disable inputs during authentication
        setInputsDisabled(true);

        // Attempt login
        authService.login(username, password).ifPresentOrElse(
            user -> {
                // ✅ Login successful
                SessionManager.setCurrentUser(user);
                System.out.println("✅ Logged in: " + user.getUsername() + " (" + user.getRole() + ")");
                
                // Trigger success callback (switch to dashboard)
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
            },
            () -> {
                // ❌ Login failed
                showError("Invalid username or password. Please try again.");
                shakeAnimation(passwordField);
                passwordField.clear();
                passwordField.requestFocus();
            }
        );

        // Re-enable inputs
        setInputsDisabled(false);
    }

    /**
     * Shows error message with animation.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        
        // Small pulse animation to draw attention
        FadeTransition ft = new FadeTransition(Duration.millis(200), errorLabel);
        ft.setFromValue(1);
        ft.setToValue(0.7);
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.play();
    }

    /**
     * Hides error message.
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    /**
     * Shake animation for invalid input.
     */
    private void shakeAnimation(TextField field) {
        field.setStyle("-fx-border-color: #dc3545; -fx-border-width: 2;");
        
        FadeTransition ft = new FadeTransition(Duration.millis(100), field);
        ft.setFromValue(1);
        ft.setToValue(0.8);
        ft.setCycleCount(3);
        ft.setAutoReverse(true);
        ft.setOnFinished(e -> field.setStyle(""));
        ft.play();
    }

    /**
     * Enable/disable all input fields.
     */
    private void setInputsDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
    }

    /**
     * Sets the callback to run after successful login.
     */
    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    /**
     * Public method to show errors from outside (optional).
     */
    public void displayError(String message) {
        showError(message);
    }

    /**
     * Clears all fields (useful for logout).
     */
    public void clearFields() {
        usernameField.clear();
        passwordField.clear();
        hideError();
    }
}