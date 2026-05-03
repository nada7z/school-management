package schoolmanagement.smproject;

import schoolmanagement.smproject.auth.controller.AuthController;
import schoolmanagement.smproject.auth.entity.User;
import schoolmanagement.smproject.common.SessionManager;
import schoolmanagement.smproject.dashboard.controller.DashboardController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

@SpringBootApplication
public class SmprojectApplication extends Application {

    private ConfigurableApplicationContext springContext;
    private Stage primaryStage;

    @Override
    public void init() {
        // 🔹 Start Spring Boot context (runs on background thread)
        springContext = new SpringApplicationBuilder(SmprojectApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 🔹 Configure main window
        configureStage(stage);

        // 🔹 Load and show login screen
        showLoginScreen();
    }

    /**
     * Configures the primary stage (window) properties.
     */
    private void configureStage(Stage stage) {
        stage.setTitle("School Management System");
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.initStyle(StageStyle.DECORATED);
        
        // Optional: Add app icon (place icon.png in resources)
        // stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        
        // 🔹 Handle window close request
        stage.setOnCloseRequest(event -> {
            event.consume(); // Prevent default close
            confirmExit(stage);
        });
    }

    /**
     * Shows the login screen.
     */
    private void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/auth.fxml")
            );
            loader.setControllerFactory(springContext::getBean);
            
            Parent loginRoot = loader.load();
            AuthController loginController = loader.getController();

            // 🔹 Set callback for successful login
            loginController.setOnLoginSuccess(() -> {
                Platform.runLater(this::showDashboardScreen);
            });

            Scene loginScene = new Scene(loginRoot);
            // Add CSS globally if needed
            // loginScene.getStylesheets().add(getClass().getResource("/styles/global.css").toExternalForm());
            
            primaryStage.setScene(loginScene);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showFatalError("Failed to load login screen: " + e.getMessage());
        }
    }

    /**
     * Shows the main dashboard after successful login.
     */
    private void showDashboardScreen() {
    try {
        // 1. Check if user is logged in
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showLoginScreen();
            return;
        }

        // 2. Load FXML from src/main/resources/view/dashboard.fxml
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/dashboard.fxml")
        );
        
        // 3. Use Spring for dependency injection
        loader.setControllerFactory(springContext::getBean);
        
        Parent dashboardRoot = loader.load();
        
        // 4. Set up Logout functionality
        DashboardController controller = loader.getController();
        controller.setOnLogout(() -> {
            Platform.runLater(this::showLoginScreen);
        });

        // 5. Set Scene and Show
        Scene dashboardScene = new Scene(dashboardRoot, 1100, 700);
        primaryStage.setScene(dashboardScene);
        primaryStage.setTitle("School Management System - Dashboard");
        primaryStage.centerOnScreen();
        
    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Error", "Failed to load dashboard: " + e.getMessage());
    }
}
    // Use the placeholder instead of loading FXML

    /**
     * Confirms exit before closing the app.
     */
    private void confirmExit(Stage stage) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved changes will be lost.");
        alert.getButtonTypes().setAll(javafx.scene.control.ButtonType.YES, javafx.scene.control.ButtonType.NO);

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.YES) {
                cleanupAndExit();
            }
        });
    }

    /**
     * Cleans up resources and exits the application.
     */
    private void cleanupAndExit() {
        // Close Spring context (releases DB connections, etc.)
        if (springContext != null) {
            springContext.close();
        }
        // Clear session
        SessionManager.logout();
        // Exit JavaFX
        Platform.exit();
        // Exit JVM (optional, Platform.exit() usually suffices)
        System.exit(0);
    }

    /**
     * Shows a simple alert dialog.
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Shows a fatal error and exits.
     */
    private void showFatalError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Fatal Error");
            alert.setHeaderText("Application cannot start");
            alert.setContentText(message + "\n\nThe application will now close.");
            alert.setOnHidden(e -> cleanupAndExit());
            alert.showAndWait();
        });
    }

    @Override
    public void stop() {
        // 🔹 Ensure cleanup if JVM shuts down unexpectedly
        cleanupAndExit();
    }

    public static void main(String[] args) {
        launch(args); // JavaFX entry point
    }
}