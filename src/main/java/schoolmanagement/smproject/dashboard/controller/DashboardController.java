package schoolmanagement.smproject.dashboard.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import schoolmanagement.smproject.common.DatabaseConnection;
import schoolmanagement.smproject.students.repository.StudentRepository;
import schoolmanagement.smproject.teachers.repository.TeacherRepository;
import schoolmanagement.smproject.courses.repository.CourseRepository;

@Component
public class DashboardController {

    // === UI ELEMENTS ===
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label totalStudentsLabel;
    @FXML
    private Label totalTeachersLabel;
    @FXML
    private Label totalCoursesLabel;
    @FXML
    private Label attendanceLabel;

    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnStudents;
    @FXML
    private Button btnTeachers;
    @FXML
    private Button btnCourses;
    @FXML
    private Button btnGrades;

    // === RECENT ACTIVITY & CHART ===
    @FXML
    private VBox activityContainer;
    @FXML
    private AreaChart<String, Number> performanceChart;
    @FXML
    private CategoryAxis chartXAxis;
    @FXML
    private NumberAxis chartYAxis;

    // === REPOSITORIES ===
    private StudentRepository studentRepo;
    private TeacherRepository teacherRepo;
    private CourseRepository courseRepo;

    @FXML
    public void initialize() {
        studentRepo = new StudentRepository();
        teacherRepo = new TeacherRepository();
        courseRepo = new CourseRepository();

        loadDashboardStats();
        updateWelcomeMessage();
        loadRecentActivities();
        loadPerformanceChart();
    }

    // === DASHBOARD STATS ===
    private void loadDashboardStats() {
        try {
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
                attendanceLabel.setText(calculateAttendance() + "%");
            }
        } catch (Exception e) {
            e.printStackTrace();
            setSafeText(totalStudentsLabel, "0");
            setSafeText(totalTeachersLabel, "0");
            setSafeText(totalCoursesLabel, "0");
            setSafeText(attendanceLabel, "0%");
        }
    }

    private int calculateAttendance() {
        return 94;
    }

    private void updateWelcomeMessage() {
        if (welcomeLabel != null && userRoleLabel != null) {
            String role = userRoleLabel.getText();
            welcomeLabel.setText("Welcome back, " + role + "!");
        }
    }

    // === RECENT ACTIVITIES ===
    private void loadRecentActivities() {
        if (activityContainer == null)
            return;
        activityContainer.getChildren().clear();

        try {
            List<ActivityItem> activities = new ArrayList<>();

            // Recent students (last 3)
            List<schoolmanagement.smproject.students.entity.Student> students = studentRepo.findAll();
            for (int i = Math.max(0, students.size() - 3); i < students.size(); i++) {
                var s = students.get(i);
                activities.add(new ActivityItem(
                        "🎓",
                        "New Student",
                        s.getFullName() + " enrolled in " + s.getGradeLevel(),
                        "Today",
                        "#4a7bc2"));
            }

            // Recent teachers (last 1)
            List<schoolmanagement.smproject.teachers.entity.Teacher> teachers = teacherRepo.findAll();
            if (!teachers.isEmpty()) {
                var t = teachers.get(teachers.size() - 1);
                activities.add(new ActivityItem(
                        "👨‍🏫",
                        "New Teacher",
                        t.getFullName() + " joined as " + t.getSubjectSpecialization(),
                        "Yesterday",
                        "#3fa37a"));
            }

            // Recent courses (last 1)
            List<schoolmanagement.smproject.courses.entity.Course> courses = courseRepo.findAll();
            if (!courses.isEmpty()) {
                var c = courses.get(courses.size() - 1);
                activities.add(new ActivityItem(
                        "📚",
                        "New Course",
                        c.getName() + " (" + c.getCourseCode() + ") created",
                        "2 days ago",
                        "#8b6fd6"));
            }

            // Add to UI (limit 5)
            activities.stream().limit(5).forEach(this::addActivityCard);

            // Fallback if empty
            if (activities.isEmpty()) {
                addActivityCard(new ActivityItem("🔔", "System Ready", "School Management System initialized",
                        "Just now", "#4a7bc2"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            addActivityCard(new ActivityItem("🔔", "System Ready", "School Management System initialized", "Just now",
                    "#4a7bc2"));
        }
    }

    private void addActivityCard(ActivityItem activity) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #f5f7fb; -fx-padding: 12 16; -fx-background-radius: 10;");

        HBox content = new HBox(12);
        content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label icon = new Label(activity.icon());
        icon.setStyle("-fx-font-size: 18; -fx-text-fill: " + activity.color() + ";");

        VBox text = new VBox(2);
        Label title = new Label(activity.title());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e2937; -fx-font-size: 14;");

        Label desc = new Label(activity.description());
        desc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");

        text.getChildren().addAll(title, desc);

        Label time = new Label(activity.time());
        time.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

        content.getChildren().addAll(icon, text);
        HBox.setHgrow(text, Priority.ALWAYS);
        content.getChildren().add(time);

        card.getChildren().add(content);
        activityContainer.getChildren().add(card);
    }

    // === PERFORMANCE CHART ===
    private void loadPerformanceChart() {
        if (performanceChart == null)
            return;

        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Student Enrollment");

            // Get real data from database
            List<Object[]> monthlyData = getEnrollmentByMonth();

            if (monthlyData.isEmpty()) {
                // Fallback sample data
                series.getData().add(new XYChart.Data<>("Jan", 45));
                series.getData().add(new XYChart.Data<>("Feb", 52));
                series.getData().add(new XYChart.Data<>("Mar", 48));
                series.getData().add(new XYChart.Data<>("Apr", 61));
                series.getData().add(new XYChart.Data<>("May", 58));
                series.getData().add(new XYChart.Data<>("Jun", 67));
            } else {
                for (Object[] row : monthlyData) {
                    series.getData().add(new XYChart.Data<>((String) row[0], (Number) row[1]));
                }
            }

            performanceChart.getData().clear();
            performanceChart.getData().add(series);
            performanceChart.setLegendVisible(false);

            // Style chart
            chartXAxis.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");
            chartYAxis.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12;");
            chartYAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(chartYAxis, null, null) {
                @Override
                public String toString(Number object) {
                    return String.valueOf(object.intValue());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            performanceChart.getData().clear();
        }
    }

    // === DATABASE QUERY: Monthly Enrollment ===
    private List<Object[]> getEnrollmentByMonth() {
        List<Object[]> results = new ArrayList<>();
        String sql = """
                    SELECT MONTHNAME(enrollment_date) as month, COUNT(*) as count
                    FROM students
                    WHERE YEAR(enrollment_date) = YEAR(CURDATE())
                    GROUP BY MONTH(enrollment_date), MONTHNAME(enrollment_date)
                    ORDER BY MONTH(enrollment_date)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(new Object[] {
                        rs.getString("month"),
                        rs.getInt("count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // === NAVIGATION HANDLERS ===
    @FXML
    private void handleDashboard() {
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
    private void handleAddStudent() {
        loadView("/studentsform.fxml");
    }

    @FXML
    private void handleAddTeacher() {
        loadView("/teachersform.fxml");
    }

    @FXML
    private void handleCreateCourse() {
        loadView("/courseform.fxml");
    }

    @FXML
    private void handleReport() {
        loadView("/reports.fxml");
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

    // === HELPERS ===

    // ✅ UPDATED: This method now preserves full-screen mode
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // ✅ Get the current stage
            Stage stage = (Stage) (btnDashboard != null ? btnDashboard.getScene().getWindow()
                    : (userRoleLabel != null ? userRoleLabel.getScene().getWindow() : null));

            if (stage != null) {
                // ✅ Save the current full-screen state BEFORE changing the scene
                boolean wasFullScreen = stage.isFullScreen();

                // Load the new scene
                stage.setScene(new Scene(root));
                stage.centerOnScreen();

                // ✅ Restore the full-screen state AFTER loading the new scene
                stage.setFullScreen(wasFullScreen);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not load: " + fxmlPath + "\n\nError: " + e.getMessage());
        }
    }

    private void setSafeText(Label label, String text) {
        if (label != null)
            label.setText(text);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // === ACTIVITY ITEM RECORD ===
    private record ActivityItem(String icon, String title, String description, String time, String color) {
    }
}