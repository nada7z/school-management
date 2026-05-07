package schoolmanagement.smproject.teachers.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.stage.Stage;

// ✅ Use YOUR existing entity and repository packages
import schoolmanagement.smproject.teachers.entity.Teacher;
import schoolmanagement.smproject.teachers.repository.TeacherRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TeachersController {

    // === FXML ELEMENTS ===
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbSubjectFilter;
    @FXML private ComboBox<String> cbStatusFilter;
    @FXML private TableView<Teacher> teachersTable;
    @FXML private TableColumn<Teacher, Integer> colId;
    @FXML private TableColumn<Teacher, String> colName;
    @FXML private TableColumn<Teacher, String> colEmail;
    @FXML private TableColumn<Teacher, String> colPhone;
    @FXML private TableColumn<Teacher, String> colSubject;
    @FXML private TableColumn<Teacher, String> colQualification;
    @FXML private TableColumn<Teacher, String> colStatus;
    @FXML private TableColumn<Teacher, Void> colActions;
    @FXML private Label lblTeacherCount;
    @FXML private Label lblPageInfo;
    @FXML private Button btnPrevious;
    @FXML private Button btnNext;

    // Sidebar Navigation
    @FXML private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnLevels, btnGrades;

    // === STATE VARIABLES ===
    private List<Teacher> allTeachers = List.of();
    private List<Teacher> filteredTeachers = List.of();
    private int currentPage = 1;
    private final int pageSize = 15;

    /**
     * Called after FXML is loaded. Sets up UI, columns, and loads initial data.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadTeachers();
        setupRealtimeSearch();
        updatePaginationUI();
    }

    /**
     * Configures TableView columns and cell factories.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colSubject.setCellValueFactory(new PropertyValueFactory<>("subjectSpecialization"));
        colQualification.setCellValueFactory(new PropertyValueFactory<>("qualification"));
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
                    Button btnEdit = new Button("✏️");
                    btnEdit.getStyleClass().addAll("action-btn-small", "action-edit");
                    btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));

                    Button btnDelete = new Button("🗑️");
                    btnDelete.getStyleClass().addAll("action-btn-small", "action-delete");
                    btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                    setGraphic(new javafx.scene.layout.HBox(6, btnEdit, btnDelete));
                }
            }
        });
    }

    /**
     * Populates filter dropdowns.
     */
    private void setupFilters() {
        cbSubjectFilter.getItems().addAll("All Subjects", "Mathematics", "French", "Science", "History", "English", "Other");
        cbSubjectFilter.getSelectionModel().selectFirst();

        cbStatusFilter.getItems().addAll("All Status", "Active", "Inactive", "On Leave");
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    /**
     * Enables real-time filtering as user types.
     */
    private void setupRealtimeSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbSubjectFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Fetches teachers from database using YOUR repository.
     */
    private void loadTeachers() {
        try {
            TeacherRepository repo = new TeacherRepository();
            allTeachers = repo.findAll();
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
            allTeachers = List.of();
            filteredTeachers = List.of();
            teachersTable.getItems().clear();
            lblTeacherCount.setText("Showing 0 teachers (Database unavailable)");
        }
    }

    /**
     * Applies search and filter criteria.
     */
    private void applyFilters() {
        String searchTerm = txtSearch.getText().toLowerCase().trim();
        String subjectFilter = cbSubjectFilter.getValue();
        String statusFilter = cbStatusFilter.getValue();

        filteredTeachers = allTeachers.stream()
            .filter(t -> {
                boolean matchesSearch = searchTerm.isEmpty() || 
                    t.getFullName().toLowerCase().contains(searchTerm) ||
                    t.getEmail().toLowerCase().contains(searchTerm) ||
                    t.getPhone().contains(searchTerm) ||
                    (t.getSubjectSpecialization() != null && t.getSubjectSpecialization().toLowerCase().contains(searchTerm)) ||
                    String.valueOf(t.getId()).contains(searchTerm);
                
                boolean matchesSubject = "All Subjects".equals(subjectFilter) || 
                    subjectFilter.equals(t.getSubjectSpecialization());
                boolean matchesStatus = "All Status".equals(statusFilter) || 
                    statusFilter.equals(t.getStatus());
                
                return matchesSearch && matchesSubject && matchesStatus;
            })
            .collect(Collectors.toList());

        currentPage = 1;
        updateTablePage();
        updatePaginationUI();
        lblTeacherCount.setText("Showing " + filteredTeachers.size() + " teacher" + (filteredTeachers.size() != 1 ? "s" : ""));
    }

    /**
     * Updates table with current page data.
     */
    private void updateTablePage() {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredTeachers.size());
        
        if (start >= filteredTeachers.size()) {
            teachersTable.getItems().clear();
        } else {
            teachersTable.getItems().setAll(filteredTeachers.subList(start, end));
        }
    }

    /**
     * Updates pagination UI state.
     */
    private void updatePaginationUI() {
        int totalPages = Math.max(1, (int) Math.ceil(filteredTeachers.size() / (double) pageSize));
        lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        btnPrevious.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    // === EVENT HANDLERS ===

    @FXML private void handleSearch() { applyFilters(); }

    @FXML private void handleClearFilters() {
        txtSearch.clear();
        cbSubjectFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @FXML private void handleAddTeacher() { loadView("/createTeacher.fxml"); }

    @FXML private void handleExport() {
        showAlert(Alert.AlertType.INFORMATION, "Export", "Export to Excel/PDF coming soon!");
    }

    @FXML private void handlePrevious() {
        if (currentPage > 1) {
            currentPage--;
            updateTablePage();
            updatePaginationUI();
        }
    }

    @FXML private void handleNext() {
        int totalPages = (int) Math.ceil(filteredTeachers.size() / (double) pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            updateTablePage();
            updatePaginationUI();
        }
    }

    private void handleEdit(Teacher teacher) {
        showAlert(Alert.AlertType.INFORMATION, "Edit Teacher", 
            "Edit form for " + teacher.getFullName() + " coming soon!\n\nID: " + teacher.getId());
    }

    private void handleDelete(Teacher teacher) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Teacher");
        alert.setHeaderText("Remove " + teacher.getFullName() + "?");
        alert.setContentText("This will NOT delete linked courses, but will remove teacher record.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                TeacherRepository repo = new TeacherRepository();
                boolean deleted = repo.deleteById(teacher.getId());
                
                if (deleted) {
                    loadTeachers();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Teacher deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "Teacher record not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
            }
        }
    }

    // === NAVIGATION ===

    @FXML private void handleDashboard() { loadView("/dashboard.fxml"); }
    @FXML private void handleStudents() { loadView("/students.fxml"); }
    @FXML private void handleTeachers() { /* Already here */ }
    @FXML private void handleCourses() { loadView("/courses.fxml"); }
    @FXML private void handleLevels() { loadView("/levels.fxml"); }
    @FXML private void handleGrades() { loadView("/grades.fxml"); }

    @FXML private void handleLogout() {
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