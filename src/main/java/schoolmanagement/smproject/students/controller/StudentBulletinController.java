package schoolmanagement.smproject.students.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.stage.Stage;
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.grades.entity.Grade;
import schoolmanagement.smproject.grades.repository.GradeRepository;

import java.io.IOException;
import java.util.List;

public class StudentBulletinController {

    @FXML private Label userRoleLabel;
    @FXML private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnLevels, btnGrades;

    @FXML private Label lblStudentName, lblStudentId, lblClassroom, lblAcademicYear;
    @FXML private Label lblTotalCoeff, lblTotalPoints, lblOverallAverage;
    @FXML private Label lblSubjectCount, lblStatusBadge, lblDecisionMessage;
    
    @FXML private TableView<Grade> gradesTable;
    @FXML private TableColumn<Grade, Integer> colIndex;
    @FXML private TableColumn<Grade, String> colSubject;
    @FXML private TableColumn<Grade, Double> colGrade;
    @FXML private TableColumn<Grade, Integer> colCoefficient;
    @FXML private TableColumn<Grade, Double> colProduct;
    @FXML private TableColumn<Grade, Void> colActions;

    private Student currentStudent;
    private GradeRepository gradeRepo;
    private final String academicYear = "2025-2026";

    @FXML
    public void initialize() {
        gradeRepo = new GradeRepository();
        setupTableColumns();
    }

    public void setStudent(Student student) {
        this.currentStudent = student;
        loadBulletin();
    }

    private void setupTableColumns() {
        gradesTable.setEditable(true);
        
        colIndex.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(gradesTable.getItems().indexOf(data.getValue()) + 1));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("average")); // Using average as base grade
        colCoefficient.setCellValueFactory(new PropertyValueFactory<>("coefficient"));
        
        colGrade.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colGrade.setOnEditCommit(event -> {
            Grade g = event.getRowValue();
            g.setAverage(event.getNewValue());
            recalculateTotals();
        });

        colCoefficient.setCellFactory(TextFieldTableCell.forTableColumn(new javafx.util.converter.IntegerStringConverter()));
        colCoefficient.setOnEditCommit(event -> {
            Grade g = event.getRowValue();
            g.setCoefficient(event.getNewValue());
            recalculateTotals();
        });

        colProduct.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyObjectWrapper<>(
            data.getValue().getAverage() != null ? data.getValue().getAverage() * data.getValue().getCoefficient() : 0.0
        ));

        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Button btnDelete = new Button("🗑️");
                    btnDelete.getStyleClass().add("btn-icon");
                    btnDelete.setOnAction(e -> {
                        gradesTable.getItems().remove(getIndex());
                        recalculateTotals();
                    });
                    setGraphic(btnDelete);
                }
            }
        });
    }

    private void loadBulletin() {
        if (currentStudent == null) return;

        lblStudentName.setText(currentStudent.getFullName());
        lblStudentId.setText("MAT-" + currentStudent.getId());
        lblClassroom.setText(currentStudent.getGradeLevel() + (currentStudent.getClassroom() != null ? "-" + currentStudent.getClassroom() : ""));
        lblAcademicYear.setText(academicYear);

        try {
            List<Grade> grades = gradeRepo.findByStudentId(currentStudent.getId());
            if (grades.isEmpty()) {
                // Add default subjects if none exist
                grades.add(new Grade(0, currentStudent.getFullName(), "Mathématiques"));
                grades.add(new Grade(0, currentStudent.getFullName(), "Physique-Chimie"));
                grades.add(new Grade(0, currentStudent.getFullName(), "Français"));
                grades.add(new Grade(0, currentStudent.getFullName(), "Anglais"));
            }
            gradesTable.getItems().setAll(grades);
            recalculateTotals();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load bulletin: " + e.getMessage());
        }
    }

    private void recalculateTotals() {
        double totalPoints = 0;
        int totalCoeff = 0;
        int subjectCount = gradesTable.getItems().size();

        for (Grade g : gradesTable.getItems()) {
            Double grade = g.getAverage();
            int coef = g.getCoefficient() > 0 ? g.getCoefficient() : 1;
            if (grade != null) {
                totalPoints += grade * coef;
                totalCoeff += coef;
            }
        }

        double average = totalCoeff > 0 ? totalPoints / totalCoeff : 0;
        
        lblTotalCoeff.setText(String.valueOf(totalCoeff));
        lblTotalPoints.setText(String.format("%.2f", totalPoints));
        lblOverallAverage.setText(String.format("%.2f / 20", average));
        lblSubjectCount.setText(subjectCount + " matières");

        // Decision logic
        if (average >= 10) {
            lblStatusBadge.setText("ADMIS(E)");
            lblStatusBadge.getStyleClass().setAll("status-badge", "status-admitted");
            lblDecisionMessage.setText(String.format("Moyenne obtenue : %.2f / 20 — Félicitations ! 🎉", average));
            lblOverallAverage.setStyle("-fx-text-fill: #10b981;");
        } else {
            lblStatusBadge.setText("NON ADMIS(E)");
            lblStatusBadge.getStyleClass().setAll("status-badge", "status-failed");
            lblDecisionMessage.setText(String.format("Moyenne obtenue : %.2f / 20 — Efforts requis 📚", average));
            lblOverallAverage.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML private void handleAddSubject() {
        Grade newGrade = new Grade(0, currentStudent.getFullName(), "Nouvelle Matière");
        newGrade.setCoefficient(1);
        newGrade.setAverage(0.0);
        gradesTable.getItems().add(newGrade);
        recalculateTotals();
    }

    @FXML private void handleReset() {
        for (Grade g : gradesTable.getItems()) {
            g.setAverage(null);
        }
        recalculateTotals();
    }

    @FXML private void handlePrint() { showAlert(Alert.AlertType.INFORMATION, "Print", "Print dialog coming soon!"); }
    @FXML private void handleExport() { showAlert(Alert.AlertType.INFORMATION, "Export", "PDF export coming soon!"); }

    @FXML private void handleBack() { loadView("/students.fxml"); }
    @FXML private void handleDashboard() { loadView("/dashboard.fxml"); }
    @FXML private void handleStudents() { loadView("/students.fxml"); }
    @FXML private void handleTeachers() { loadView("/teachers.fxml"); }
    @FXML private void handleCourses() { loadView("/courses.fxml"); }
    @FXML private void handleLevels() { loadView("/levels.fxml"); }
    @FXML private void handleGrades() { loadView("/grades.fxml"); }

    @FXML private void handleLogout() {
        if (new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?").showAndWait().get() == ButtonType.YES) {
            loadView("/login.fxml");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) lblStudentName.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load: " + fxmlPath);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }
}