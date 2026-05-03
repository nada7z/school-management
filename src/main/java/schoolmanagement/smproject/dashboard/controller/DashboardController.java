package schoolmanagement.smproject.dashboard.controller;

import schoolmanagement.smproject.common.SessionManager;
import schoolmanagement.smproject.students.controller.CreateStudentController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.io.IOException;
import javafx.scene.Node;
import org.springframework.stereotype.Component;
import javafx.fxml.FXMLLoader;

@Component
public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label attendanceLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnStudents;
    @FXML private Button btnTeachers;
    @FXML private Button btnCourses;
    @FXML private Button btnGrades;

    private Runnable onLogout;

    @FXML
    public void initialize() {
        System.out.println("✅ DashboardController initialize() called");

        var user = SessionManager.getCurrentUser();
        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome back, " + user.getUsername() + "!");
        }

        updateStats();
        setActiveButton(btnDashboard);
    }

    private void updateStats() {
        if (totalStudentsLabel != null) totalStudentsLabel.setText("0");
        if (totalTeachersLabel != null) totalTeachersLabel.setText("0");
        if (totalCoursesLabel != null) totalCoursesLabel.setText("0");
        if (attendanceLabel != null) attendanceLabel.setText("0%");
    }

    @FXML private void handleDashboard() { setActiveButton(btnDashboard); }
    @FXML private void handleStudents() { setActiveButton(btnStudents); }
    @FXML private void handleTeachers() { setActiveButton(btnTeachers); }
    @FXML private void handleCourses() { setActiveButton(btnCourses); }
    @FXML private void handleGrades() { setActiveButton(btnGrades); }

    @FXML private void handleAddTeacher() { System.out.println("Add Teacher clicked"); }
    @FXML private void handleCreateCourse() { System.out.println("Create Course clicked"); }
    @FXML private void handleReport() { System.out.println("Generate Report clicked"); }

    @FXML private void handleLogout() {
        SessionManager.logout();
        if (onLogout != null) onLogout.run();
    }

    private void setActiveButton(Button btn) {
        resetButton(btnDashboard);
        resetButton(btnStudents);
        resetButton(btnTeachers);
        resetButton(btnCourses);
        resetButton(btnGrades);
        if (btn != null) btn.getStyleClass().add("active");
    }

    private void resetButton(Button btn) {
        if (btn != null) btn.getStyleClass().remove("active");
    }

    public void setOnLogout(Runnable callback) {
        this.onLogout = callback;
    }

    // 🔽 THIS IS THE FUNCTION THAT OPENS THE FORM
    @FXML
    private void handleAddStudent() {
    try {
        System.out.println("Button clicked!");

        var url = getClass().getResource("/studentsform.fxml");
        System.out.println("FXML URL = " + url);

        if (url == null) {
            throw new RuntimeException("studentsform.fxml not found in resources");
        }

        FXMLLoader loader = new FXMLLoader(url);
        javafx.scene.Parent root = loader.load();

        Stage stage = (Stage) btnDashboard.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // 🔽 Helper to show alerts
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleOpenPage(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main/resources/studentsform.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();

    } catch (IOException e) {
        e.printStackTrace();
    }
    }
}