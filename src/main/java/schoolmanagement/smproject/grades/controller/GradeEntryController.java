package schoolmanagement.smproject.grades.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.stage.Stage;

import schoolmanagement.smproject.grades.entity.GradeEntry;
import schoolmanagement.smproject.grades.entity.Grade;
import schoolmanagement.smproject.grades.repository.GradeRepository;
import schoolmanagement.smproject.students.repository.StudentRepository;
import schoolmanagement.smproject.students.entity.Student;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GradeEntryController {

    @FXML private Label userRoleLabel;
    @FXML private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnLevels, btnGrades;
    @FXML private Label subjectTitle;
    @FXML private TableView<GradeEntry> gradesTable;
    @FXML private TableColumn<GradeEntry, String> colStudentName;
    @FXML private TableColumn<GradeEntry, Double> colTest1;
    @FXML private TableColumn<GradeEntry, Double> colTest2;
    @FXML private TableColumn<GradeEntry, Double> colExam;
    @FXML private TableColumn<GradeEntry, Double> colAverage;

    private GradeRepository gradeRepo;
    private StudentRepository studentRepo;

    private String currentLevel;
    private int currentLevelId;
    private String currentSubject;
    private final String academicYear = "2025-2026";
    private List<GradeEntry> gradeEntries = new ArrayList<>();

    @FXML
    public void initialize() {
        gradeRepo = new GradeRepository();
        studentRepo = new StudentRepository();
        setupTableColumns();
    }

    public void setLevelAndSubject(String level, int levelId, String subject) {
        this.currentLevel = level;
        this.currentLevelId = levelId;
        this.currentSubject = subject;
        if (subjectTitle != null) {
            subjectTitle.setText(level + " - " + subject);
        }
        loadGradesFromDatabase();
    }

    private void setupTableColumns() {
        gradesTable.setEditable(true);

        // ✅ Student Name Column (StringProperty)
        colStudentName.setCellValueFactory(data -> data.getValue().studentNameProperty());

        // ✅ Test 1 Column (DoubleProperty) - FIXED
        colTest1.setCellValueFactory(data -> data.getValue().test1Property().asObject());
        colTest1.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colTest1.setOnEditCommit(event -> {
            GradeEntry entry = event.getRowValue();
            entry.setTest1(event.getNewValue());
            calculateAverage(entry);
            gradesTable.refresh();
        });

        // ✅ Test 2 Column (DoubleProperty) - FIXED
        colTest2.setCellValueFactory(data -> data.getValue().test2Property().asObject());
        colTest2.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colTest2.setOnEditCommit(event -> {
            GradeEntry entry = event.getRowValue();
            entry.setTest2(event.getNewValue());
            calculateAverage(entry);
            gradesTable.refresh();
        });

        // ✅ Exam Column (DoubleProperty) - FIXED
        colExam.setCellValueFactory(data -> data.getValue().examProperty().asObject());
        colExam.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colExam.setOnEditCommit(event -> {
            GradeEntry entry = event.getRowValue();
            entry.setExam(event.getNewValue());
            calculateAverage(entry);
            gradesTable.refresh();
        });

        // ✅ Average Column (DoubleProperty, Read-only)
        colAverage.setCellValueFactory(data -> data.getValue().averageProperty().asObject());
        colAverage.setEditable(false);
    }

    private void calculateAverage(GradeEntry entry) {
        Double test1 = entry.getTest1();
        Double test2 = entry.getTest2();
        Double exam = entry.getExam();

        if (test1 != null && test2 != null && exam != null) {
            double avg = (test1 + test2 + exam) / 3.0;
            entry.setAverage(Math.round(avg * 10.0) / 10.0); // Round to 1 decimal
        } else {
            entry.setAverage(null);
        }
    }

    private void loadGradesFromDatabase() {
        try {
            // 1️⃣ Get all students in this level
            List<Student> students = studentRepo.findByGradeLevel(currentLevel);
            
            // 2️⃣ Get existing grades for this level + subject
            List<Grade> existingGrades = gradeRepo.findByLevelAndSubject(currentLevelId, currentSubject);
            
            // 3️⃣ Build GradeEntry list for TableView
            gradeEntries.clear();
            for (Student student : students) {
                GradeEntry entry = new GradeEntry(student.getFullName());
                entry.setStudentId(student.getId());
                entry.setLevel(currentLevel);
                entry.setSubject(currentSubject);
                
                // Find existing grade if any
                for (Grade grade : existingGrades) {
                    if (grade.getStudentId() == student.getId()) {
                        entry.setTest1(grade.getTest1());
                        entry.setTest2(grade.getTest2());
                        entry.setExam(grade.getExam());
                        entry.setAverage(grade.getAverage());
                        break;
                    }
                }
                gradeEntries.add(entry);
            }
            
            gradesTable.getItems().setAll(gradeEntries);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", 
                "Failed to load grades: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveAll() {
        try {
            int savedCount = 0;
            
            for (GradeEntry entry : gradeEntries) {
                if (entry.getTest1() != null || entry.getTest2() != null || entry.getExam() != null) {
                    Grade grade = new Grade();
                    grade.setStudentId(entry.getStudentId());
                    grade.setLevelId(currentLevelId);
                    grade.setSubject(currentSubject);
                    grade.setTest1(entry.getTest1());
                    grade.setTest2(entry.getTest2());
                    grade.setExam(entry.getExam());
                    grade.setAcademicYear(academicYear);
                    
                    gradeRepo.save(grade);
                    savedCount++;
                }
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Success ✅", 
                "Grades saved successfully!\n" + savedCount + " student(s) updated.");
            
            // Reload to reflect any DB-calculated values
            loadGradesFromDatabase();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error ❌", 
                "Failed to save grades: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/subjects.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) subjectTitle.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // === NAVIGATION ===
    @FXML private void handleDashboard() { loadView("/dashboard.fxml"); }
    @FXML private void handleStudents() { loadView("/students.fxml"); }
    @FXML private void handleTeachers() { loadView("/teachers.fxml"); }
    @FXML private void handleCourses() { loadView("/courses.fxml"); }
    @FXML private void handleLevels() { loadView("/levels.fxml"); }
    @FXML private void handleGrades() { }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?");
        if (alert.showAndWait().get() == ButtonType.YES) {
            loadView("/login.fxml");
        }
    }

    // === HELPERS ===
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) (subjectTitle != null ? subjectTitle.getScene().getWindow() : btnGrades.getScene().getWindow());
            stage.setScene(new Scene(root));
        } catch (IOException e) {
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