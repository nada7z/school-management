package schoolmanagement.smproject.dashboard.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DashboardController {

    // === UI ELEMENTS (Match dashboard.fxml fx:id) ===
    
    // Labels
    @FXML private Label userRoleLabel;
    @FXML private Label welcomeLabel;
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalTeachersLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label attendanceLabel;

    // Navigation Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnStudents;
    @FXML private Button btnTeachers;
    @FXML private Button btnCourses;
    @FXML private Button btnGrades;

    /**
     * Called automatically after FXML is loaded.
     * Initializes stats and UI state.
     */
    @FXML
    public void initialize() {
        loadDashboardStats();
        updateWelcomeMessage();
    }

    /**
     * Loads statistics from repositories and updates UI labels.
     */
    private void loadDashboardStats() {
        try {
            // Import repositories dynamically to avoid circular dependency issues
            var studentRepo = new schoolmanagement.smproject.students.repository.StudentRepository();
            var teacherRepo = new schoolmanagement.smproject.teachers.repository.TeacherRepository();
            var courseRepo = new schoolmanagement.smproject.courses.repository.CourseRepository();

            if (totalStudentsLabel != null) {
                totalStudentsLabel.setText(String.valueOf(studentRepo.countAll()));
            }
            if (totalTeachersLabel != null) {
                totalTeachersLabel.setText(String.valueOf(teacherRepo.countAll()));
            }
            if (totalCoursesLabel != null) {
                totalCoursesLabel.setText(String.valueOf(courseRepo.countActive()));
            }
            if (attendanceLabel != null) {
                // TODO: Replace with real attendance calculation
                attendanceLabel.setText("94%"); 
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback values if DB connection fails
            setSafeText(totalStudentsLabel, "0");
            setSafeText(totalTeachersLabel, "0");
            setSafeText(totalCoursesLabel, "0");
            setSafeText(attendanceLabel, "0%");
        }
    }

    /**
     * Updates the welcome message with the user's role.
     */
    private void updateWelcomeMessage() {
        if (welcomeLabel != null && userRoleLabel != null) {
            String role = userRoleLabel.getText();
            welcomeLabel.setText("Welcome back, " + role + "!");
        }
    }

    // === NAVIGATION HANDLERS (Sidebar Buttons) ===

    @FXML
    private void handleDashboard() {
        // Already on dashboard, just refresh stats
        loadDashboardStats();
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
        loadView("/grades.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        if (alert.showAndWait().get() == ButtonType.YES) {
            loadView("/login.fxml"); 
        }
    }

    // === QUICK ACTION HANDLERS (Main Content Buttons) ===

    @FXML
    private void handleAddStudent() {
        // Navigate to the Create Student Form
        loadView("/studentsform.fxml");
    }

    @FXML
    private void handleAddTeacher() {
        // Navigate to the Create Teacher Form
        loadView("/teachersform.fxml");
    }

    @FXML
    private void handleCreateCourse() {
        // Navigate to the Create Course Form
        loadView("/courseform.fxml");
    }

    @FXML
    private void handleReport() {
        // TODO: Implement report generation logic
        showAlert(Alert.AlertType.INFORMATION, "Report Generation", "Report feature coming soon!");
    }

    // === HELPER METHODS ===

    /**
     * Helper to load and switch to a different FXML view.
     * @param fxmlPath The path to the FXML file relative to resources.
     */
    protected void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) (btnDashboard != null ? btnDashboard.getScene().getWindow() : 
                                  (userRoleLabel != null ? userRoleLabel.getScene().getWindow() : null));
            
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                "Could not load: " + fxmlPath + "\n\nError: " + e.getMessage());
        }
    }

    /**
     * Safely sets text on a label if it's not null.
     */
    private void setSafeText(Label label, String text) {
        if (label != null) {
            label.setText(text);
        }
    }

    /**
     * Shows a simple alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}