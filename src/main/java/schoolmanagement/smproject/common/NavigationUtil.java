package schoolmanagement.smproject.common;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Centralized navigation utility to preserve full screen mode across page changes.
 */
public class NavigationUtil {
    
    private static Stage mainStage;
    private static boolean isFullScreen = false;
    
    /**
     * Initialize the main stage (call this once from your main application class)
     */
    public static void init(Stage stage) {
        mainStage = stage;
    }
    
    /**
     * Navigate to a new page while preserving full screen mode
     */
    public static void navigate(String fxmlPath) {
        try {
            if (mainStage == null) {
                System.err.println("NavigationUtil: Stage not initialized!");
                return;
            }
            
            // Save current full screen state
            isFullScreen = mainStage.isFullScreen();
            
            // Load new FXML
            FXMLLoader loader = new FXMLLoader(NavigationUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            // Set new scene
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.centerOnScreen();
            
            // Restore full screen state
            mainStage.setFullScreen(isFullScreen);
            
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Navigation failed: " + fxmlPath);
        }
    }
    
    /**
     * Get the main stage
     */
    public static Stage getStage() {
        return mainStage;
    }
}