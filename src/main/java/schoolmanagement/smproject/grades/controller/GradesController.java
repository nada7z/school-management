package schoolmanagement.smproject.grades.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import schoolmanagement.smproject.classes.entity.Level;
import schoolmanagement.smproject.grades.controller.SubjectsController;

import java.io.IOException;

public class GradesController {

    @FXML
    private Label userRoleLabel;
    @FXML
    private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnLevels, btnGrades;

    @FXML
    private void handleCE1() {
        navigateToSubjects("CE1", 1);
    }

    @FXML
    private void handleCE2() {
        navigateToSubjects("CE2", 2);
    }

    @FXML
    private void handleCE3() {
        navigateToSubjects("CE3", 3);
    }

    @FXML
    private void handleCE4() {
        navigateToSubjects("CE4", 4);
    }

    @FXML
    private void handleCE5() {
        navigateToSubjects("CE5", 5);
    }

    @FXML
    private void handleCE6() {
        navigateToSubjects("CE6", 6);
    }

    private void navigateToSubjects(String levelName, int levelId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subjects.fxml"));
            Parent root = loader.load();

            SubjectsController controller = loader.getController();
            controller.setLevel(levelName, levelId);

            Stage stage = (Stage) btnGrades.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("School MS ➜ " + levelName + " Subjects");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load subjects: " + e.getMessage());
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