package schoolmanagement.smproject.students.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.stage.Stage;
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.students.repository.StudentRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class StudentsController {

    // === FXML ELEMENTS ===
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbGradeFilter;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, Integer> colId;
    @FXML private TableColumn<Student, String> colName;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colPhone;
    @FXML private TableColumn<Student, String> colGrade;
    @FXML private TableColumn<Student, String> colClassroom;
    @FXML private TableColumn<Student, String> colParent;
    @FXML private TableColumn<Student, String> colStatus;
    @FXML private TableColumn<Student, Void> colActions;
    @FXML private Label lblStudentCount;
    @FXML private Label lblPageInfo;
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;

    // Sidebar Navigation
    @FXML private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnLevels, btnGrades;

    // === STATE VARIABLES ===
    private List<Student> allStudents = List.of();
    private List<Student> filteredStudents = List.of();
    private int currentPage = 1;
    private final int pageSize = 15;

    /**
     * Called after FXML is loaded. Sets up UI, columns, and loads initial data.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadStudents();
        setupRealtimeSearch();
        updatePaginationUI();
    }

    /**
     * Configures TableView columns, cell factories, and status badges.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("gradeLevel"));
        
        // Classroom might not be in Student entity yet. Safely fallback to N/A
        colClassroom.setCellValueFactory(cell -> new ReadOnlyStringWrapper("N/A"));
        
        colParent.setCellValueFactory(cell -> {
            Student s = cell.getValue();
            String parent = (s.getPrimaryParent() != null) ? s.getPrimaryParent().getFullName() : "N/A";
            return new ReadOnlyStringWrapper(parent);
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status Badge Cell Factory
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null); setGraphic(null);
                } else {
                    Label badge = new Label(status.toUpperCase());
                    badge.getStyleClass().addAll("status-badge", "status-" + status.toLowerCase());
                    setText(null); setGraphic(badge);
                }
            }
        });

        // Action Buttons Cell Factory
        colActions.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button btnEdit = new Button("✎");
                    btnEdit.getStyleClass().addAll("action-btn-small", "action-edit");
                    btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));

                    Button btnDelete = new Button("✖");
                    btnDelete.getStyleClass().addAll("action-btn-small", "action-delete");
                    btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                    setGraphic(new javafx.scene.layout.HBox(6, btnEdit, btnDelete));
                }
            }
        });
    }

    /**
     * Populates filter dropdowns with default options.
     */
    private void setupFilters() {
        cbGradeFilter.getItems().addAll("All Grades", "CE1", "CE2", "CE3", "CE4", "CE5", "CE6");
        cbGradeFilter.getSelectionModel().selectFirst();

        cbStatusFilter.getItems().addAll("All Status", "Active", "Inactive", "Suspended");
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    /**
     * Enables real-time filtering as user types in search box.
     */
    private void setupRealtimeSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbGradeFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Fetches students from database and applies initial filters.
     */
    private void loadStudents() {
        try {
            StudentRepository repo = new StudentRepository();
            allStudents = repo.findAll();
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
            allStudents = List.of();
            filteredStudents = List.of();
            studentsTable.getItems().clear();
            lblStudentCount.setText("Showing 0 students (Database unavailable)");
        }
    }

    /**
     * Applies search text, grade filter, and status filter to the dataset.
     */
    private void applyFilters() {
        String searchTerm = txtSearch.getText().toLowerCase().trim();
        String gradeFilter = cbGradeFilter.getValue();
        String statusFilter = cbStatusFilter.getValue();

        filteredStudents = allStudents.stream()
            .filter(s -> {
                boolean matchesSearch = searchTerm.isEmpty() || 
                    s.getFullName().toLowerCase().contains(searchTerm) ||
                    s.getEmail().toLowerCase().contains(searchTerm) ||
                    String.valueOf(s.getId()).contains(searchTerm);
                
                boolean matchesGrade = "All Grades".equals(gradeFilter) || gradeFilter.equals(s.getGradeLevel());
                boolean matchesStatus = "All Status".equals(statusFilter) || statusFilter.equals(s.getStatus());
                
                return matchesSearch && matchesGrade && matchesStatus;
            })
            .collect(Collectors.toList());

        currentPage = 1;
        updateTablePage();
        updatePaginationUI();
        lblStudentCount.setText("Showing " + filteredStudents.size() + " student" + (filteredStudents.size() != 1 ? "s" : ""));
    }

    /**
     * Updates the TableView to show only the current page's data.
     */
    private void updateTablePage() {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredStudents.size());
        
        if (start >= filteredStudents.size()) {
            studentsTable.getItems().clear();
        } else {
            studentsTable.getItems().setAll(filteredStudents.subList(start, end));
        }
    }

    /**
     * Updates pagination labels and enables/disables navigation buttons.
     */
    private void updatePaginationUI() {
        int totalPages = Math.max(1, (int) Math.ceil(filteredStudents.size() / (double) pageSize));
        lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        
        btnPrevious.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    // === EVENT HANDLERS ===

    @FXML
    private void handleSearch() { applyFilters(); }

    @FXML
    private void handleClearFilters() {
        txtSearch.clear();
        cbGradeFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAddStudent() { loadView("/studentsform.fxml"); }

    @FXML
    private void handleExport() {
        showAlert(Alert.AlertType.INFORMATION, "Export", "Export to Excel/PDF coming soon!");
    }

    @FXML
    private void handlePrevious() {
        if (currentPage > 1) {
            currentPage--;
            updateTablePage();
            updatePaginationUI();
        }
    }

    @FXML
    private void handleNext() {
        int totalPages = (int) Math.ceil(filteredStudents.size() / (double) pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            updateTablePage();
            updatePaginationUI();
        }
    }

    private void handleEdit(Student student) {
        showAlert(Alert.AlertType.INFORMATION, "Edit Student", "Edit form for " + student.getFullName() + " coming soon!");
        // TODO: Navigate to edit form with student data
    }

    private void handleDelete(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Student");
        alert.setHeaderText("Remove " + student.getFullName() + "?");
        alert.setContentText("This action cannot be undone.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                new StudentRepository().deleteById(student.getId());
                loadStudents(); // Refresh list
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student: " + e.getMessage());
            }
        }
    }

    // === NAVIGATION ===

    @FXML private void handleDashboard() { loadView("/dashboard.fxml"); }
    @FXML private void handleStudents() { /* Already here */ }
    @FXML private void handleTeachers() { loadView("/teachers.fxml"); }
    @FXML private void handleCourses() { loadView("/courses.fxml"); }
    @FXML private void handleLevels() { loadView("/levels.fxml"); }
    @FXML private void handleGrades() { loadView("/grades.fxml"); }

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
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
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