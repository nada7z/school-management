package schoolmanagement.smproject.students.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

// ✅ CORRECT IMPORTS - Use .model package
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.parents.entity.Parent;
import schoolmanagement.smproject.students.repository.StudentRepository;
import schoolmanagement.smproject.parents.repository.ParentRepository;

public class CreateStudentController {

    // ===== NAVIGATION BUTTONS (from sidebar) =====
    @FXML private Label userRoleLabel;
    @FXML private Button btnDashboard;
    @FXML private Button btnStudents;
    @FXML private Button btnTeachers;
    @FXML private Button btnCourses;
    @FXML private Button btnGrades;

    // ===== STUDENT FIELDS =====
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private DatePicker dpDateOfBirth;
    @FXML private ComboBox<String> cbGender;
    @FXML private ComboBox<String> cbGradeLevel;
    @FXML private TextArea txtAddress;

    // ===== PRIMARY PARENT FIELDS =====
    @FXML private TextField txtParent1FirstName;
    @FXML private TextField txtParent1LastName;
    @FXML private ComboBox<String> cbParent1Relationship;
    @FXML private TextField txtParent1Email;
    @FXML private TextField txtParent1Phone;
    @FXML private TextField txtParent1PhoneAlt;
    @FXML private TextField txtParent1Occupation;
    @FXML private TextArea txtParent1Address;
    @FXML private CheckBox chkParent1Primary;

    // ===== SECONDARY PARENT FIELDS =====
    @FXML private TextField txtParent2FirstName;
    @FXML private TextField txtParent2LastName;
    @FXML private ComboBox<String> cbParent2Relationship;
    @FXML private TextField txtParent2Email;
    @FXML private TextField txtParent2Phone;
    @FXML private TextField txtParent2Occupation;

    // ===== EMERGENCY CONTACT FIELDS =====
    @FXML private TextField txtEmergencyName;
    @FXML private TextField txtEmergencyPhone;
    @FXML private ComboBox<String> cbEmergencyRelationship;

    // ===== STATE =====
    private Stage dashboardStage;

    // ===== INITIALIZATION =====
    @FXML
    public void initialize() {
        // Populate ComboBoxes (FXML already has items, but this ensures fallback)
        if (cbGender.getItems().isEmpty()) {
            cbGender.getItems().addAll("Male", "Female");
        }
        if (cbGradeLevel.getItems().isEmpty()) {
            cbGradeLevel.getItems().addAll("CE1", "CE2", "CE3", "CE4", "CE5", "CE6");
        }
        if (cbParent1Relationship.getItems().isEmpty()) {
            cbParent1Relationship.getItems().addAll("Father", "Mother", "Guardian", "Step-Parent", "Other");
        }
        if (cbParent2Relationship.getItems().isEmpty()) {
            cbParent2Relationship.getItems().addAll("Father", "Mother", "Guardian", "Step-Parent", "Other");
        }
        if (cbEmergencyRelationship.getItems().isEmpty()) {
            cbEmergencyRelationship.getItems().addAll("Parent", "Relative", "Family Friend", "Neighbor", "Other");
        }
        
        // Set defaults
        cbGender.getSelectionModel().selectFirst();
        cbGradeLevel.getSelectionModel().selectFirst();
        cbParent1Relationship.getSelectionModel().select("Father");
        cbParent2Relationship.getSelectionModel().select("Mother");
        cbEmergencyRelationship.getSelectionModel().select("Parent");
        chkParent1Primary.setSelected(true);
        
        addValidationListeners();
    }

    public void setDashboardStage(Stage stage) {
        this.dashboardStage = stage;
    }

    // ===== NAVIGATION HANDLERS =====
    @FXML private void handleDashboard() { loadView("/dashboard.fxml"); }
    @FXML private void handleStudents() { loadView("/students.fxml"); }
    @FXML private void handleTeachers() { loadView("/teachers.fxml"); }
    @FXML private void handleCourses() { loadView("/courses.fxml"); }
    @FXML private void handleGrades() { loadView("/grades.fxml"); }

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

    // ===== FORM ACTIONS =====
    @FXML
    private void handleSaveStudent() {
        if (!validateForm()) return;

        try {
            // 1️⃣ Create Student Object
            Student student = new Student();
            student.setFirstName(txtFirstName.getText().trim());
            student.setLastName(txtLastName.getText().trim());
            student.setEmail(txtEmail.getText().trim());
            student.setPhone(txtPhone.getText().trim());
            student.setDateOfBirth(dpDateOfBirth.getValue());
            student.setGender(cbGender.getValue());
            student.setAddress(txtAddress.getText().trim());
            student.setGradeLevel(cbGradeLevel.getValue());
            student.setEnrollmentDate(LocalDate.now());
            student.setStatus("Active");

            // 2️⃣ Create & Save Primary Parent
            Parent primaryParent = new Parent();
            primaryParent.setFirstName(txtParent1FirstName.getText().trim());
            primaryParent.setLastName(txtParent1LastName.getText().trim());
            primaryParent.setRelationship(cbParent1Relationship.getValue());
            primaryParent.setEmail(txtParent1Email.getText().trim());
            primaryParent.setPhone(txtParent1Phone.getText().trim());
            primaryParent.setPhoneAlternate(txtParent1PhoneAlt.getText().trim());
            primaryParent.setOccupation(txtParent1Occupation.getText().trim());
            primaryParent.setAddress(txtParent1Address.getText().trim());
            primaryParent.setPrimaryContact(chkParent1Primary.isSelected());

            ParentRepository parentRepo = new ParentRepository();
            
            // Check if parent already exists by email to avoid duplicates
            var existingPrimary = parentRepo.findByEmail(primaryParent.getEmail());
            if (existingPrimary.isPresent() && !existingPrimary.get().getEmail().isEmpty()) {
                primaryParent = existingPrimary.get();
            } else {
                primaryParent = parentRepo.save(primaryParent);
            }
            student.setPrimaryParent(primaryParent);

            // 3️⃣ Save Secondary Parent (if provided)
            if (!txtParent2FirstName.getText().trim().isEmpty()) {
                Parent secondaryParent = new Parent();
                secondaryParent.setFirstName(txtParent2FirstName.getText().trim());
                secondaryParent.setLastName(txtParent2LastName.getText().trim());
                secondaryParent.setRelationship(cbParent2Relationship.getValue());
                secondaryParent.setEmail(txtParent2Email.getText().trim());
                secondaryParent.setPhone(txtParent2Phone.getText().trim());
                secondaryParent.setOccupation(txtParent2Occupation.getText().trim());
                
                var existingSecondary = parentRepo.findByEmail(secondaryParent.getEmail());
                if (existingSecondary.isPresent() && !existingSecondary.get().getEmail().isEmpty()) {
                    secondaryParent = existingSecondary.get();
                } else {
                    secondaryParent = parentRepo.save(secondaryParent);
                }
                student.setSecondaryParent(secondaryParent);
            }

            // 4️⃣ Set Emergency Contact
            student.setEmergencyContactName(txtEmergencyName.getText().trim());
            student.setEmergencyContactPhone(txtEmergencyPhone.getText().trim());
            student.setEmergencyContactRelationship(cbEmergencyRelationship.getValue());

            // 5️⃣ Save Student
            StudentRepository studentRepo = new StudentRepository();
            Student savedStudent = studentRepo.save(student);

            // 6️⃣ Show Success
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success ✅");
            alert.setHeaderText("Student Registered!");
            alert.setContentText(savedStudent.getFullName() + " (ID: " + savedStudent.getId() + ") has been saved to database.");
            alert.showAndWait();
            
            handleReset();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error ❌");
            alert.setHeaderText("Failed to save student");
            alert.setContentText("Error: " + e.getMessage() + "\n\nCheck console for details.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel");
        alert.setHeaderText("Discard Changes?");
        alert.setContentText("All unsaved data will be lost.");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        if (alert.showAndWait().get() == ButtonType.YES) {
            handleStudents();
        }
    }

    @FXML
    private void handleReset() {
        // Student
        txtFirstName.clear(); txtLastName.clear(); txtEmail.clear(); txtPhone.clear();
        dpDateOfBirth.setValue(null);
        cbGender.getSelectionModel().clearSelection();
        cbGradeLevel.getSelectionModel().clearSelection();
        txtAddress.clear();

        // Primary Parent
        txtParent1FirstName.clear(); txtParent1LastName.clear();
        cbParent1Relationship.getSelectionModel().clearSelection();
        txtParent1Email.clear(); txtParent1Phone.clear(); txtParent1PhoneAlt.clear();
        txtParent1Occupation.clear(); txtParent1Address.clear();
        chkParent1Primary.setSelected(true);

        // Secondary Parent
        txtParent2FirstName.clear(); txtParent2LastName.clear();
        cbParent2Relationship.getSelectionModel().clearSelection();
        txtParent2Email.clear(); txtParent2Phone.clear(); txtParent2Occupation.clear();

        // Emergency
        txtEmergencyName.clear(); txtEmergencyPhone.clear();
        cbEmergencyRelationship.getSelectionModel().clearSelection();

        txtFirstName.requestFocus();
    }

    // ===== VALIDATION =====
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        clearErrorStyles();

        // Student Required
        if (isEmpty(txtFirstName)) { errors.append("• First Name is required\n"); setError(txtFirstName); }
        if (isEmpty(txtLastName)) { errors.append("• Last Name is required\n"); setError(txtLastName); }
        if (dpDateOfBirth.getValue() == null) { errors.append("• Date of Birth is required\n"); setError(dpDateOfBirth); }
        if (cbGender.getValue() == null) { errors.append("• Gender is required\n"); setError(cbGender); }
        if (cbGradeLevel.getValue() == null) { errors.append("• Grade Level is required\n"); setError(cbGradeLevel); }

        // Primary Parent Required
        if (isEmpty(txtParent1FirstName)) { errors.append("• Parent First Name is required\n"); setError(txtParent1FirstName); }
        if (isEmpty(txtParent1LastName)) { errors.append("• Parent Last Name is required\n"); setError(txtParent1LastName); }
        if (cbParent1Relationship.getValue() == null) { errors.append("• Parent Relationship is required\n"); setError(cbParent1Relationship); }
        if (isEmpty(txtParent1Phone)) { errors.append("• Parent Phone is required\n"); setError(txtParent1Phone); }

        // Emergency Required
        if (isEmpty(txtEmergencyName)) { errors.append("• Emergency Name is required\n"); setError(txtEmergencyName); }
        if (isEmpty(txtEmergencyPhone)) { errors.append("• Emergency Phone is required\n"); setError(txtEmergencyPhone); }
        if (cbEmergencyRelationship.getValue() == null) { errors.append("• Emergency Relationship is required\n"); setError(cbEmergencyRelationship); }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errors.toString());
            return false;
        }

        // Email format check
        if (!txtEmail.getText().trim().isEmpty() && !isValidEmail(txtEmail.getText())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address.");
            setError(txtEmail);
            return false;
        }

        return true;
    }

    private boolean isEmpty(TextField field) {
        return field.getText() == null || field.getText().trim().isEmpty();
    }

    private void setError(TextField field) {
        field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
    }

    private void setError(DatePicker field) {
        field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
    }

    private void setError(ComboBox<?> field) {
        field.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
    }

    private void clearErrorStyles() {
        txtFirstName.setStyle(""); txtLastName.setStyle(""); txtEmail.setStyle(""); txtPhone.setStyle("");
        dpDateOfBirth.setStyle(""); cbGender.setStyle(""); cbGradeLevel.setStyle("");
        txtParent1FirstName.setStyle(""); txtParent1LastName.setStyle(""); cbParent1Relationship.setStyle("");
        txtParent1Phone.setStyle(""); txtEmergencyName.setStyle(""); txtEmergencyPhone.setStyle("");
        cbEmergencyRelationship.setStyle("");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== REAL-TIME VALIDATION LISTENERS =====
    private void addValidationListeners() {
        txtFirstName.textProperty().addListener((obs, old, val) -> { if (!val.trim().isEmpty()) txtFirstName.setStyle(""); });
        txtLastName.textProperty().addListener((obs, old, val) -> { if (!val.trim().isEmpty()) txtLastName.setStyle(""); });
        txtParent1FirstName.textProperty().addListener((obs, old, val) -> { if (!val.trim().isEmpty()) txtParent1FirstName.setStyle(""); });
        txtParent1LastName.textProperty().addListener((obs, old, val) -> { if (!val.trim().isEmpty()) txtParent1LastName.setStyle(""); });
        txtParent1Phone.textProperty().addListener((obs, old, val) -> { if (!val.trim().isEmpty()) txtParent1Phone.setStyle(""); });
        txtEmergencyName.textProperty().addListener((obs, old, val) -> { if (!val.trim().isEmpty()) txtEmergencyName.setStyle(""); });
        txtEmergencyPhone.textProperty().addListener((obs, old, val) -> { if (!val.trim().isEmpty()) txtEmergencyPhone.setStyle(""); });
    }

    // ===== NAVIGATION HELPER (FIXED) =====
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            
            Stage stage = (Stage) (btnDashboard != null ? btnDashboard.getScene().getWindow() : 
                                  (userRoleLabel != null ? userRoleLabel.getScene().getWindow() : null));
            
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load: " + fxmlPath + "\n\n" + e.getMessage());
        }
    }
}