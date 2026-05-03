package schoolmanagement.smproject.students.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.stage.Stage;
import schoolmanagement.smproject.classes.entity.Classroom;
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.students.repository.StudentRepository;

import java.io.IOException;

public class ClassStudentsController {

    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colPhone;
    @FXML private TableColumn<Student, String> colParent;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private Label classTitle;
    @FXML private Button btnBack;

    private Classroom currentClass;

    // 🔹 AUTOMATICALLY CALLED BY FXMLLOADER AFTER INJECTING @FXML FIELDS
    @FXML
    public void initialize() {
        setupTableColumns();
    }

    // 🔹 CALLED BY ClassesController IMMEDIATELY AFTER LOADING THIS FXML
    public void setCurrentClass(Classroom classroom) {
        this.currentClass = classroom;
        if (currentClass != null) {
            classTitle.setText("👨‍🎓 " + currentClass.getFullName());
            loadStudents();
        }
    }

    // 🔹 1. SETUP COLUMNS & BIND DATA
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        colParent.setCellValueFactory(cell -> {
            Student s = cell.getValue();
            String pName = (s.getPrimaryParent() != null) ? s.getPrimaryParent().getFullName() : "N/A";
            return new ReadOnlyStringWrapper(pName);
        });

        colStatus.setCellValueFactory(cell -> {
            String status = cell.getValue().getStatus() != null ? cell.getValue().getStatus() : "Active";
            return new ReadOnlyStringWrapper(status);
        });

        // 🔹 2. CUSTOM CELL FACTORY FOR COLORED STATUS BADGES
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().addAll("status-badge", "status-" + status.toLowerCase());
                    setText(null);
                    setGraphic(badge);
                }
            }
        });
    }

    // 🔹 3. FETCH DATA FROM DATABASE
    private void loadStudents() {
        studentsTable.getItems().clear();
        try {
            StudentRepository repo = new StudentRepository();
            studentsTable.getItems().setAll(repo.findByClassroomId(currentClass.getId()));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load students: " + e.getMessage());
        }
    }

    // 🔹 NAVIGATION
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/classes.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not return to classes view.");
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