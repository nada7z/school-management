package schoolmanagement.smproject.grades.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class SubjectsController {

    @FXML
    private Label userRoleLabel;
    @FXML
    private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnLevels, btnGrades;
    @FXML
    private Label levelTitle;

    private String currentLevelName;
    private int currentLevelId;

    public void setLevel(String levelName, int levelId) {
        this.currentLevelName = levelName;
        this.currentLevelId = levelId;
        if (levelTitle != null) {
            levelTitle.setText(levelName + " - Subjects");
        }
    }

    @FXML
    private void handleMath() {
        navigateToGradeEntry("Mathematics");
    }

    @FXML
    private void handleFrench() {
        navigateToGradeEntry("French");
    }

    @FXML
    private void handleArabic() {
        navigateToGradeEntry("Arabic");
    }

    @FXML
    private void handleEnglish() {
        navigateToGradeEntry("English");
    }

    @FXML
    private void handleScience() {
        navigateToGradeEntry("Science");
    }

    @FXML
    private void handleHistory() {
        navigateToGradeEntry("History");
    }

    private void navigateToGradeEntry(String subject) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gradeEntry.fxml"));
            Parent root = loader.load();

            GradeEntryController controller = loader.getController();
            controller.setLevelAndSubject(currentLevelName, currentLevelId, subject);

            Stage stage = (Stage) levelTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("School MS ➜ " + currentLevelName + " - " + subject);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load grade entry: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/grades.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) levelTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDashboard() {
        loadView("/dashboard.fxml");
    }

    @FXML
    private void handleStudents() {
        loadView("/students.fxml");
    }

    @FXML
    private void handleTeachers() {
        loadView("/teachers.fxml");
    }

    @FXML
    private void handleCourses() {
        loadView("/courses.fxml");
    }

    @FXML
    private void handleLevels() {
        loadView("/levels.fxml");
    }

    @FXML
    private void handleGrades() {
        /* Already here */ }

    @FXML
    private void handleReport() {
        loadView("/reports.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?");
        if (alert.showAndWait().get() == javafx.scene.control.ButtonType.YES) {
            loadView("/login.fxml");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) btnDashboard.getScene().getWindow();

            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();

            double width = stage.getWidth();
            double height = stage.getHeight();
            double x = stage.getX();
            double y = stage.getY();

            stage.setScene(new Scene(root));

            stage.setX(x);
            stage.setY(y);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMaximized(wasMaximized);
            stage.setFullScreen(wasFullScreen);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load: " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}