package schoolmanagement.smproject.students.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.parents.entity.Parent;
import schoolmanagement.smproject.students.repository.StudentRepository;
import schoolmanagement.smproject.parents.repository.ParentRepository;

public class CreateStudentController {
    // ===== NAVIGATION BUTTONS =====
    @FXML
    private Label userRoleLabel;
    @FXML
    private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnGrades;

    // ===== STUDENT FIELDS =====
    @FXML
    private TextField txtFirstName, txtLastName, txtEmail, txtPhone;
    @FXML
    private DatePicker dpDateOfBirth;
    @FXML
    private ComboBox<String> cbGender, cbGradeLevel;
    @FXML
    private TextArea txtAddress;

    // ===== PRIMARY PARENT FIELDS =====
    @FXML
    private TextField txtParent1FirstName, txtParent1LastName, txtParent1Email, txtParent1Phone, txtParent1PhoneAlt,
            txtParent1Occupation;
    @FXML
    private ComboBox<String> cbParent1Relationship;
    @FXML
    private TextArea txtParent1Address;
    @FXML
    private CheckBox chkParent1Primary;

    // ===== SECONDARY PARENT FIELDS =====
    @FXML
    private TextField txtParent2FirstName, txtParent2LastName, txtParent2Email, txtParent2Phone, txtParent2Occupation;
    @FXML
    private ComboBox<String> cbParent2Relationship;

    // ===== EMERGENCY CONTACT FIELDS =====
    @FXML
    private TextField txtEmergencyName, txtEmergencyPhone;
    @FXML
    private ComboBox<String> cbEmergencyRelationship;

    // ===== STATE =====
    private Stage dashboardStage;
    private Student studentToEdit;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        if (cbGender.getItems().isEmpty())
            cbGender.getItems().addAll("Male", "Female", "Other", "Prefer not to say");
        if (cbGradeLevel.getItems().isEmpty())
            cbGradeLevel.getItems().addAll("CE1", "CE2", "CE3", "CE4", "CE5", "CE6");
        if (cbParent1Relationship.getItems().isEmpty())
            cbParent1Relationship.getItems().addAll("Father", "Mother", "Guardian", "Step-Parent", "Other");
        if (cbParent2Relationship.getItems().isEmpty())
            cbParent2Relationship.getItems().addAll("Father", "Mother", "Guardian", "Step-Parent", "Other");
        if (cbEmergencyRelationship.getItems().isEmpty())
            cbEmergencyRelationship.getItems().addAll("Parent", "Relative", "Family Friend", "Neighbor", "Other");

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

    public void setStudentToEdit(Student student) {
        this.studentToEdit = student;
        this.isEditMode = student != null;
        if (isEditMode)
            populateFormWithStudentData();
    }

    private void populateFormWithStudentData() {
        if (studentToEdit == null)
            return;
        txtFirstName.setText(studentToEdit.getFirstName());
        txtLastName.setText(studentToEdit.getLastName());
        txtEmail.setText(studentToEdit.getEmail());
        txtPhone.setText(studentToEdit.getPhone());
        dpDateOfBirth.setValue(studentToEdit.getDateOfBirth());
        cbGender.setValue(studentToEdit.getGender());
        cbGradeLevel.setValue(studentToEdit.getGradeLevel());
        txtAddress.setText(studentToEdit.getAddress());

        if (studentToEdit.getPrimaryParent() != null) {
            Parent p = studentToEdit.getPrimaryParent();
            txtParent1FirstName.setText(p.getFirstName());
            txtParent1LastName.setText(p.getLastName());
            cbParent1Relationship.setValue(p.getRelationship());
            txtParent1Email.setText(p.getEmail());
            txtParent1Phone.setText(p.getPhone());
            txtParent1PhoneAlt.setText(p.getPhoneAlternate());
            txtParent1Occupation.setText(p.getOccupation());
            txtParent1Address.setText(p.getAddress());
            chkParent1Primary.setSelected(p.isPrimaryContact());
        }

        if (studentToEdit.getSecondaryParent() != null) {
            Parent p = studentToEdit.getSecondaryParent();
            txtParent2FirstName.setText(p.getFirstName());
            txtParent2LastName.setText(p.getLastName());
            cbParent2Relationship.setValue(p.getRelationship());
            txtParent2Email.setText(p.getEmail());
            txtParent2Phone.setText(p.getPhone());
            txtParent2Occupation.setText(p.getOccupation());
        }

        txtEmergencyName.setText(studentToEdit.getEmergencyContactName());
        txtEmergencyPhone.setText(studentToEdit.getEmergencyContactPhone());
        cbEmergencyRelationship.setValue(studentToEdit.getEmergencyContactRelationship());
    }

    // ===== NAVIGATION =====
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
        if (alert.showAndWait().get() == ButtonType.YES)
            loadView("/login.fxml");
    }

    // ===== FORM ACTIONS =====
    @FXML
    private void handleSaveStudent() {
        if (!validateForm())
            return;

        try {
            StudentRepository studentRepo = new StudentRepository();
            ParentRepository parentRepo = new ParentRepository();
            Student student;

            if (isEditMode && studentToEdit != null) {
                student = studentToEdit;

                student.setFirstName(txtFirstName.getText().trim());
                student.setLastName(txtLastName.getText().trim());
                student.setEmail(txtEmail.getText().trim());
                student.setPhone(txtPhone.getText().trim());
                student.setDateOfBirth(dpDateOfBirth.getValue());
                student.setGender(cbGender.getValue());
                student.setAddress(txtAddress.getText().trim());
                student.setGradeLevel(cbGradeLevel.getValue());

                // ===== PRIMARY PARENT =====
                Parent primaryParent = student.getPrimaryParent();

                if (primaryParent == null) {
                    primaryParent = new Parent();
                }

                primaryParent.setFirstName(txtParent1FirstName.getText().trim());
                primaryParent.setLastName(txtParent1LastName.getText().trim());
                primaryParent.setRelationship(cbParent1Relationship.getValue());
                primaryParent.setEmail(txtParent1Email.getText().trim());
                primaryParent.setPhone(txtParent1Phone.getText().trim());
                primaryParent.setPhoneAlternate(txtParent1PhoneAlt.getText().trim());
                primaryParent.setOccupation(txtParent1Occupation.getText().trim());
                primaryParent.setAddress(txtParent1Address.getText().trim());
                primaryParent.setPrimaryContact(chkParent1Primary.isSelected());

                // IMPORTANT: save/update parent BEFORE linking it to student
                if (primaryParent.getId() > 0) {
                    primaryParent = parentRepo.update(primaryParent);
                } else {
                    primaryParent = parentRepo.save(primaryParent);
                }

                student.setPrimaryParent(primaryParent);

                // ===== SECONDARY PARENT =====
                if (!txtParent2FirstName.getText().trim().isEmpty()) {
                    Parent secondaryParent = student.getSecondaryParent();

                    if (secondaryParent == null) {
                        secondaryParent = new Parent();
                    }

                    secondaryParent.setFirstName(txtParent2FirstName.getText().trim());
                    secondaryParent.setLastName(txtParent2LastName.getText().trim());
                    secondaryParent.setRelationship(cbParent2Relationship.getValue());
                    secondaryParent.setEmail(txtParent2Email.getText().trim());
                    secondaryParent.setPhone(txtParent2Phone.getText().trim());
                    secondaryParent.setOccupation(txtParent2Occupation.getText().trim());

                    if (secondaryParent.getId() > 0) {
                        secondaryParent = parentRepo.update(secondaryParent);
                    } else {
                        secondaryParent = parentRepo.save(secondaryParent);
                    }

                    student.setSecondaryParent(secondaryParent);
                } else {
                    student.setSecondaryParent(null);
                }

                student.setEmergencyContactName(txtEmergencyName.getText().trim());
                student.setEmergencyContactPhone(txtEmergencyPhone.getText().trim());
                student.setEmergencyContactRelationship(cbEmergencyRelationship.getValue());

                studentRepo.update(student);

                showAlert(Alert.AlertType.INFORMATION, "Success ✅", "Student updated successfully!");

            } else {
                // ===== CREATE NEW STUDENT =====

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

                Optional<Parent> existingByEmail = parentRepo.findByEmail(primaryParent.getEmail());

                if (existingByEmail.isPresent()) {
                    primaryParent = existingByEmail.get();
                } else {
                    Optional<Parent> existingByPhone = parentRepo.findByPhone(primaryParent.getPhone());

                    if (existingByPhone.isPresent()) {
                        primaryParent = existingByPhone.get();
                    } else {
                        primaryParent = parentRepo.save(primaryParent);
                    }
                }

                Parent secondaryParent = null;

                if (!txtParent2FirstName.getText().trim().isEmpty()) {
                    secondaryParent = new Parent();
                    secondaryParent.setFirstName(txtParent2FirstName.getText().trim());
                    secondaryParent.setLastName(txtParent2LastName.getText().trim());
                    secondaryParent.setRelationship(cbParent2Relationship.getValue());
                    secondaryParent.setEmail(txtParent2Email.getText().trim());
                    secondaryParent.setPhone(txtParent2Phone.getText().trim());
                    secondaryParent.setOccupation(txtParent2Occupation.getText().trim());

                    Optional<Parent> existingSecEmail = parentRepo.findByEmail(secondaryParent.getEmail());

                    if (existingSecEmail.isPresent()) {
                        secondaryParent = existingSecEmail.get();
                    } else {
                        Optional<Parent> existingSecPhone = parentRepo.findByPhone(secondaryParent.getPhone());

                        if (existingSecPhone.isPresent()) {
                            secondaryParent = existingSecPhone.get();
                        } else {
                            secondaryParent = parentRepo.save(secondaryParent);
                        }
                    }
                }

                student = new Student();
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

                student.setPrimaryParent(primaryParent);
                student.setSecondaryParent(secondaryParent);

                student.setEmergencyContactName(txtEmergencyName.getText().trim());
                student.setEmergencyContactPhone(txtEmergencyPhone.getText().trim());
                student.setEmergencyContactRelationship(cbEmergencyRelationship.getValue());

                Student savedStudent = studentRepo.save(student);

                showAlert(Alert.AlertType.INFORMATION, "Success ✅",
                        "Student Registered!\n" + savedStudent.getFullName() + " (ID: " + savedStudent.getId() + ")");
            }

            handleReset();
            handleStudents();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error ❌", "Failed to save student:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "All unsaved data will be lost.", ButtonType.YES,
                ButtonType.NO);
        alert.setTitle("Discard Changes?");
        alert.setHeaderText(null);
        if (alert.showAndWait().get() == ButtonType.YES)
            handleStudents();
    }

    @FXML
    private void handleReset() {
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtPhone.clear();
        dpDateOfBirth.setValue(null);
        cbGender.getSelectionModel().clearSelection();
        cbGradeLevel.getSelectionModel().clearSelection();
        txtAddress.clear();
        txtParent1FirstName.clear();
        txtParent1LastName.clear();
        cbParent1Relationship.getSelectionModel().clearSelection();
        txtParent1Email.clear();
        txtParent1Phone.clear();
        txtParent1PhoneAlt.clear();
        txtParent1Occupation.clear();
        txtParent1Address.clear();
        chkParent1Primary.setSelected(true);
        txtParent2FirstName.clear();
        txtParent2LastName.clear();
        cbParent2Relationship.getSelectionModel().clearSelection();
        txtParent2Email.clear();
        txtParent2Phone.clear();
        txtParent2Occupation.clear();
        txtEmergencyName.clear();
        txtEmergencyPhone.clear();
        cbEmergencyRelationship.getSelectionModel().clearSelection();
        txtFirstName.requestFocus();
    }

    // ===== VALIDATION =====
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        clearErrorStyles();
        if (isEmpty(txtFirstName)) {
            errors.append("• First Name\n");
            setError(txtFirstName);
        }
        if (isEmpty(txtLastName)) {
            errors.append("• Last Name\n");
            setError(txtLastName);
        }
        if (dpDateOfBirth.getValue() == null) {
            errors.append("• Date of Birth\n");
            setError(dpDateOfBirth);
        }
        if (cbGender.getValue() == null) {
            errors.append("• Gender\n");
            setError(cbGender);
        }
        if (cbGradeLevel.getValue() == null) {
            errors.append("• Grade Level\n");
            setError(cbGradeLevel);
        }
        if (isEmpty(txtParent1FirstName)) {
            errors.append("• Parent First Name\n");
            setError(txtParent1FirstName);
        }
        if (isEmpty(txtParent1LastName)) {
            errors.append("• Parent Last Name\n");
            setError(txtParent1LastName);
        }
        if (cbParent1Relationship.getValue() == null) {
            errors.append("• Parent Relationship\n");
            setError(cbParent1Relationship);
        }
        if (isEmpty(txtParent1Phone)) {
            errors.append("• Parent Phone\n");
            setError(txtParent1Phone);
        }
        if (isEmpty(txtEmergencyName)) {
            errors.append("• Emergency Name\n");
            setError(txtEmergencyName);
        }
        if (isEmpty(txtEmergencyPhone)) {
            errors.append("• Emergency Phone\n");
            setError(txtEmergencyPhone);
        }
        if (cbEmergencyRelationship.getValue() == null) {
            errors.append("• Emergency Relationship\n");
            setError(cbEmergencyRelationship);
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Missing Fields", "Please fill in:\n" + errors.toString());
            return false;
        }
        if (!txtEmail.getText().trim().isEmpty() && !isValidEmail(txtEmail.getText())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address.");
            setError(txtEmail);
            return false;
        }
        return true;
    }

    private boolean isEmpty(TextField f) {
        return f.getText() == null || f.getText().trim().isEmpty();
    }

    private void setError(TextField f) {
        f.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
    }

    private void setError(DatePicker f) {
        f.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
    }

    private void setError(ComboBox<?> f) {
        f.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px;");
    }

    private void clearErrorStyles() {
        txtFirstName.setStyle("");
        txtLastName.setStyle("");
        txtEmail.setStyle("");
        txtPhone.setStyle("");
        dpDateOfBirth.setStyle("");
        cbGender.setStyle("");
        cbGradeLevel.setStyle("");
        txtParent1FirstName.setStyle("");
        txtParent1LastName.setStyle("");
        cbParent1Relationship.setStyle("");
        txtParent1Phone.setStyle("");
        txtEmergencyName.setStyle("");
        txtEmergencyPhone.setStyle("");
        cbEmergencyRelationship.setStyle("");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    // ✅ FIXED showAlert METHOD
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void addValidationListeners() {
        txtFirstName.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty())
                txtFirstName.setStyle("");
        });
        txtLastName.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty())
                txtLastName.setStyle("");
        });
        txtParent1FirstName.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty())
                txtParent1FirstName.setStyle("");
        });
        txtParent1LastName.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty())
                txtParent1LastName.setStyle("");
        });
        txtParent1Phone.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty())
                txtParent1Phone.setStyle("");
        });
        txtEmergencyName.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty())
                txtEmergencyName.setStyle("");
        });
        txtEmergencyPhone.textProperty().addListener((obs, old, val) -> {
            if (!val.trim().isEmpty())
                txtEmergencyPhone.setStyle("");
        });
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            Stage stage = (Stage) (btnDashboard != null ? btnDashboard.getScene().getWindow()
                    : userRoleLabel.getScene().getWindow());
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load: " + fxmlPath);
        }
    }
}