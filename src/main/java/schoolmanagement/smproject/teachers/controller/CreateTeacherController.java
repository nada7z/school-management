package schoolmanagement.smproject.teachers.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import schoolmanagement.smproject.teachers.entity.Teacher;
import schoolmanagement.smproject.teachers.repository.TeacherRepository;

import java.io.IOException;
import java.time.LocalDate;

public class CreateTeacherController {

    @FXML
    private Label userRoleLabel;
    @FXML
    private Button btnDashboard, btnTeachers, btnCourses, btnStudents;

    @FXML
    private TextField txtFirstName, txtLastName, txtEmail, txtPhone;
    @FXML
    private TextField txtEmployeeId, txtSubject, txtQualification;
    @FXML
    private TextField txtEmergencyName, txtEmergencyPhone;
    @FXML
    private TextArea txtAddress;
    @FXML
    private DatePicker dpDateOfBirth;
    @FXML
    private ComboBox<String> cbGender, cbStatus;
    @FXML
    private Teacher editingTeacher = null;

    @FXML
    public void initialize() {
        cbGender.getItems().addAll("Male", "Female", "Other");
        cbStatus.getItems().addAll("Active", "Inactive", "On Leave");
        cbStatus.getSelectionModel().select("Active");
    }

    @FXML
    private void handleSaveTeacher() {
        if (!validateForm())
            return;

        try {
            Teacher teacher = editingTeacher != null ? editingTeacher : new Teacher();

            teacher.setEmployeeId(txtEmployeeId.getText().trim());
            teacher.setFirstName(txtFirstName.getText().trim());
            teacher.setLastName(txtLastName.getText().trim());
            teacher.setEmail(txtEmail.getText().trim());
            teacher.setPhone(txtPhone.getText().trim());
            teacher.setDateOfBirth(dpDateOfBirth.getValue());
            teacher.setGender(cbGender.getValue());
            teacher.setAddress(txtAddress.getText().trim());
            teacher.setSubjectSpecialization(txtSubject.getText().trim());
            teacher.setQualification(txtQualification.getText().trim());
            teacher.setStatus(cbStatus.getValue());
            teacher.setEmergencyContactName(txtEmergencyName.getText().trim());
            teacher.setEmergencyContactPhone(txtEmergencyPhone.getText().trim());

            if (editingTeacher == null) {
                teacher.setHireDate(LocalDate.now());
            }

            TeacherRepository repo = new TeacherRepository();

            if (editingTeacher == null) {
                repo.save(teacher);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher added successfully!");
            } else {
                repo.update(teacher);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher updated successfully!");
            }

            handleTeachers();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save teacher: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtFirstName.getText().trim().isEmpty())
            errors.append("• First Name is required\n");
        if (txtLastName.getText().trim().isEmpty())
            errors.append("• Last Name is required\n");
        if (txtSubject.getText().trim().isEmpty())
            errors.append("• Subject Specialization is required\n");

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errors.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void handleReset() {
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtEmployeeId.clear();
        txtSubject.clear();
        txtQualification.clear();
        txtEmergencyName.clear();
        txtEmergencyPhone.clear();
        txtAddress.clear();
        dpDateOfBirth.setValue(null);
        cbGender.getSelectionModel().clearSelection();
        cbStatus.getSelectionModel().select("Active");
        txtFirstName.requestFocus();
    }

    @FXML
    private void handleCancel() {
        handleTeachers();
    }

    @FXML
    private void handleDashboard() {
        loadView("/dashboard.fxml");
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
    private void handleStudents() {
        loadView("/students.fxml");
    }

    @FXML
    private void handleGrades() {
        loadView("/grades.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?");
        if (alert.showAndWait().get() == ButtonType.YES)
            loadView("/login.fxml");
    }

    public void setTeacherForEdit(Teacher teacher) {
        this.editingTeacher = teacher;

        txtEmployeeId.setText(teacher.getEmployeeId());
        txtFirstName.setText(teacher.getFirstName());
        txtLastName.setText(teacher.getLastName());
        txtEmail.setText(teacher.getEmail());
        txtPhone.setText(teacher.getPhone());
        dpDateOfBirth.setValue(teacher.getDateOfBirth());
        cbGender.getSelectionModel().select(teacher.getGender());
        txtAddress.setText(teacher.getAddress());
        txtSubject.setText(teacher.getSubjectSpecialization());
        txtQualification.setText(teacher.getQualification());
        cbStatus.getSelectionModel().select(teacher.getStatus());
        txtEmergencyName.setText(teacher.getEmergencyContactName());
        txtEmergencyPhone.setText(teacher.getEmergencyContactPhone());
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
        alert.setContentText(msg);
        alert.showAndWait();
    }
}