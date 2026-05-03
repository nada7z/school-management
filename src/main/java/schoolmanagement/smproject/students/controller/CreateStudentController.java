package schoolmanagement.smproject.students.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.parents.entity.Parent;

public class CreateStudentController {

    // ===== NAVIGATION BUTTONS =====
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

    // ===== NAVIGATION HANDLERS =====
    @FXML
    private void handleDashboard() {
        loadView("/dashboard/dashboard.fxml");
    }

    @FXML
    private void handleStudents() {
        loadView("/students/studentsList.fxml");
    }

    @FXML
    private void handleTeachers() {
        loadView("/teachers/teachersList.fxml");
    }

    @FXML
    private void handleCourses() {
        loadView("/courses/coursesList.fxml");
    }

    @FXML
    private void handleGrades() {
        loadView("/grades/gradesList.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        if (alert.showAndWait().get() == ButtonType.YES) {
            loadView("/login/login.fxml");
        }
    }

    // ===== FORM ACTIONS =====
    @FXML
    private void handleSaveStudent() {
        if (!validateForm()) {
            return;
        }

        // Create Student Object
        Student student = new Student();
        student.setFirstName(txtFirstName.getText().trim());
        student.setLastName(txtLastName.getText().trim());
        student.setEmail(txtEmail.getText().trim());
        student.setPhone(txtPhone.getText().trim());
        student.setDateOfBirth(dpDateOfBirth.getValue());
        student.setGender(cbGender.getValue());
        student.setAddress(txtAddress.getText().trim());
        student.setGradeLevel(cbGradeLevel.getValue());

        // Create Primary Parent
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
        student.setPrimaryParent(primaryParent);

        // Create Secondary Parent (if filled)
        if (!txtParent2FirstName.getText().trim().isEmpty()) {
            Parent secondaryParent = new Parent();
            secondaryParent.setFirstName(txtParent2FirstName.getText().trim());
            secondaryParent.setLastName(txtParent2LastName.getText().trim());
            secondaryParent.setRelationship(cbParent2Relationship.getValue());
            secondaryParent.setEmail(txtParent2Email.getText().trim());
            secondaryParent.setPhone(txtParent2Phone.getText().trim());
            secondaryParent.setOccupation(txtParent2Occupation.getText().trim());
            student.setSecondaryParent(secondaryParent);
        }

        // Emergency Contact
        student.setEmergencyContactName(txtEmergencyName.getText().trim());
        student.setEmergencyContactPhone(txtEmergencyPhone.getText().trim());
        student.setEmergencyContactRelationship(cbEmergencyRelationship.getValue());

        // TODO: Save to database via service layer
        // studentService.saveStudent(student);

        // Show Success Alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Student Added Successfully!");
        alert.setContentText(student.getFullName() + " has been registered.");
        alert.getButtonTypes().setAll(ButtonType.OK);
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            handleReset();
            // Optionally navigate to student list
            // handleStudents();
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
        // Student Fields
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtPhone.clear();
        dpDateOfBirth.setValue(null);
        cbGender.getSelectionModel().clearSelection();
        cbGradeLevel.getSelectionModel().clearSelection();
        txtAddress.clear();

        // Primary Parent
        txtParent1FirstName.clear();
        txtParent1LastName.clear();
        cbParent1Relationship.getSelectionModel().clearSelection();
        txtParent1Email.clear();
        txtParent1Phone.clear();
        txtParent1PhoneAlt.clear();
        txtParent1Occupation.clear();
        txtParent1Address.clear();
        chkParent1Primary.setSelected(true);

        // Secondary Parent
        txtParent2FirstName.clear();
        txtParent2LastName.clear();
        cbParent2Relationship.getSelectionModel().clearSelection();
        txtParent2Email.clear();
        txtParent2Phone.clear();
        txtParent2Occupation.clear();

        // Emergency Contact
        txtEmergencyName.clear();
        txtEmergencyPhone.clear();
        cbEmergencyRelationship.getSelectionModel().clearSelection();

        // Focus first field
        txtFirstName.requestFocus();
    }

    // ===== VALIDATION =====
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Student Required Fields
        if (txtFirstName.getText().trim().isEmpty()) {
            errors.append("• First Name is required\n");
            txtFirstName.setStyle("-fx-border-color: #ef4444;");
        } else {
            txtFirstName.setStyle("");
        }

        if (txtLastName.getText().trim().isEmpty()) {
            errors.append("• Last Name is required\n");
            txtLastName.setStyle("-fx-border-color: #ef4444;");
        } else {
            txtLastName.setStyle("");
        }

        if (dpDateOfBirth.getValue() == null) {
            errors.append("• Date of Birth is required\n");
            dpDateOfBirth.setStyle("-fx-border-color: #ef4444;");
        } else {
            dpDateOfBirth.setStyle("");
        }

        if (cbGender.getValue() == null) {
            errors.append("• Gender is required\n");
            cbGender.setStyle("-fx-border-color: #ef4444;");
        } else {
            cbGender.setStyle("");
        }

        if (cbGradeLevel.getValue() == null) {
            errors.append("• Grade Level is required\n");
            cbGradeLevel.setStyle("-fx-border-color: #ef4444;");
        } else {
            cbGradeLevel.setStyle("");
        }

        // Primary Parent Required Fields
        if (txtParent1FirstName.getText().trim().isEmpty()) {
            errors.append("• Parent First Name is required\n");
            txtParent1FirstName.setStyle("-fx-border-color: #ef4444;");
        } else {
            txtParent1FirstName.setStyle("");
        }

        if (txtParent1LastName.getText().trim().isEmpty()) {
            errors.append("• Parent Last Name is required\n");
            txtParent1LastName.setStyle("-fx-border-color: #ef4444;");
        } else {
            txtParent1LastName.setStyle("");
        }

        if (cbParent1Relationship.getValue() == null) {
            errors.append("• Parent Relationship is required\n");
            cbParent1Relationship.setStyle("-fx-border-color: #ef4444;");
        } else {
            cbParent1Relationship.setStyle("");
        }

        if (txtParent1Phone.getText().trim().isEmpty()) {
            errors.append("• Parent Primary Phone is required\n");
            txtParent1Phone.setStyle("-fx-border-color: #ef4444;");
        } else {
            txtParent1Phone.setStyle("");
        }

        // Emergency Contact Required Fields
        if (txtEmergencyName.getText().trim().isEmpty()) {
            errors.append("• Emergency Contact Name is required\n");
            txtEmergencyName.setStyle("-fx-border-color: #ef4444;");
        } else {
            txtEmergencyName.setStyle("");
        }

        if (txtEmergencyPhone.getText().trim().isEmpty()) {
            errors.append("• Emergency Contact Phone is required\n");
            txtEmergencyPhone.setStyle("-fx-border-color: #ef4444;");
        } else {
            txtEmergencyPhone.setStyle("");
        }

        if (cbEmergencyRelationship.getValue() == null) {
            errors.append("• Emergency Relationship is required\n");
            cbEmergencyRelationship.setStyle("-fx-border-color: #ef4444;");
        } else {
            cbEmergencyRelationship.setStyle("");
        }

        // Show errors if any
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please fix the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        // Email validation (optional)
        if (!txtEmail.getText().trim().isEmpty() && !isValidEmail(txtEmail.getText())) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Email");
            alert.setContentText("Please enter a valid email address.");
            alert.showAndWait();
            txtEmail.setStyle("-fx-border-color: #ef4444;");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // ===== HELPER METHOD =====
    private void loadView(String fxmlPath) {
        try {
            // TODO: Implement proper scene switching via MainApp or NavigationService
            // FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            // Parent root = loader.load();
            // Scene scene = new Scene(root);
            // MainApp.getPrimaryStage().setScene(scene);
            System.out.println("Navigate to: " + fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Navigation Error", "Could not load: " + fxmlPath);
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== INITIALIZATION =====
    @FXML
    public void initialize() {
        // Set default selections
        cbGender.getSelectionModel().selectFirst();
        cbGradeLevel.getSelectionModel().selectFirst();
        cbParent1Relationship.getSelectionModel().select("Father");
        cbParent2Relationship.getSelectionModel().select("Mother");
        cbEmergencyRelationship.getSelectionModel().select("Parent");
        
        // Add listeners for real-time validation styling
        addValidationListeners();
    }

    private void addValidationListeners() {
        // Student fields
        txtFirstName.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.trim().isEmpty()) txtFirstName.setStyle("");
        });
        txtLastName.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.trim().isEmpty()) txtLastName.setStyle("");
        });
        // Add similar listeners for other required fields...
    }

    public void setDashboardStage(Stage stage) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setDashboardStage'");
    }


}