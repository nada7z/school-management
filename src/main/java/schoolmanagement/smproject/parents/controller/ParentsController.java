package schoolmanagement.smproject.parents.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.stage.Stage;
import schoolmanagement.smproject.parents.entity.Parent;
import schoolmanagement.smproject.parents.repository.ParentRepository;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ParentsController {
    // === FXML ELEMENTS ===
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbRelationshipFilter;
    @FXML
    private TableView<Parent> parentsTable;
    @FXML
    private TableColumn<Parent, Integer> colId;
    @FXML
    private TableColumn<Parent, String> colName;
    @FXML
    private TableColumn<Parent, String> colEmail;
    @FXML
    private TableColumn<Parent, String> colPhone;
    @FXML
    private TableColumn<Parent, String> colRelationship;
    @FXML
    private TableColumn<Parent, String> colOccupation;
    @FXML
    private TableColumn<Parent, String> colStatus;
    @FXML
    private TableColumn<Parent, Void> colActions;
    @FXML
    private Label lblParentCount;
    @FXML
    private Label lblPageInfo;
    @FXML
    private Button btnPrevious;
    @FXML
    private Button btnNext;

    // Sidebar Navigation
    @FXML
    private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnLevels, btnGrades;

    // === STATE VARIABLES ===
    private List<Parent> allParents = List.of();
    private List<Parent> filteredParents = List.of();
    private int currentPage = 1;
    private final int pageSize = 15;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadParents();
        setupRealtimeSearch();
        updatePaginationUI();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colRelationship.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        colOccupation.setCellValueFactory(new PropertyValueFactory<>("occupation"));

        colStatus.setCellValueFactory(cell -> {
            Parent p = cell.getValue();
            String status = p.isPrimaryContact() ? "Primary" : "Secondary";
            return new ReadOnlyStringWrapper(status);
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status.toUpperCase());
                    badge.getStyleClass().addAll("status-badge",
                            status.equalsIgnoreCase("Primary") ? "status-active" : "status-inactive");
                    setText(null);
                    setGraphic(badge);
                }
            }
        });

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

    private void setupFilters() {
        cbRelationshipFilter.getItems().addAll("All", "Father", "Mother", "Guardian", "Step-Parent", "Other");
        cbRelationshipFilter.getSelectionModel().selectFirst();
    }

    private void setupRealtimeSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbRelationshipFilter.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadParents() {
        try {
            ParentRepository repo = new ParentRepository();
            allParents = repo.findAll();
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
            allParents = List.of();
            filteredParents = List.of();
            parentsTable.getItems().clear();
            lblParentCount.setText("Showing 0 parents (Database unavailable)");
        }
    }

    private void applyFilters() {
        String searchTerm = txtSearch.getText().toLowerCase().trim();
        String relationshipFilter = cbRelationshipFilter.getValue();

        filteredParents = allParents.stream()
                .filter(p -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            p.getFullName().toLowerCase().contains(searchTerm) ||
                            p.getEmail().toLowerCase().contains(searchTerm) ||
                            p.getPhone().contains(searchTerm) ||
                            String.valueOf(p.getId()).contains(searchTerm);

                    boolean matchesRelationship = "All".equals(relationshipFilter) ||
                            relationshipFilter.equals(p.getRelationship());

                    return matchesSearch && matchesRelationship;
                })
                .collect(Collectors.toList());

        currentPage = 1;
        updateTablePage();
        updatePaginationUI();
        lblParentCount
                .setText("Showing " + filteredParents.size() + " parent" + (filteredParents.size() != 1 ? "s" : ""));
    }

    private void updateTablePage() {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredParents.size());

        if (start >= filteredParents.size()) {
            parentsTable.getItems().clear();
        } else {
            parentsTable.getItems().setAll(filteredParents.subList(start, end));
        }
    }

    private void updatePaginationUI() {
        int totalPages = Math.max(1, (int) Math.ceil(filteredParents.size() / (double) pageSize));
        lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        btnPrevious.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        txtSearch.clear();
        cbRelationshipFilter.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAddParent() {
        loadView("/createParent.fxml");
    }

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
        int totalPages = (int) Math.ceil(filteredParents.size() / (double) pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            updateTablePage();
            updatePaginationUI();
        }
    }

    private void handleEdit(Parent parent) {
        showAlert(Alert.AlertType.INFORMATION, "Edit Parent",
                "Edit form for " + parent.getFullName() + " coming soon!\n\nID: " + parent.getId());
    }

    private void handleDelete(Parent parent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Parent");
        alert.setHeaderText("Remove " + parent.getFullName() + "?");
        alert.setContentText("This will NOT delete linked students, but will remove parent contact info.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                ParentRepository repo = new ParentRepository();
                boolean deleted = repo.deleteById(parent.getId());

                if (deleted) {
                    loadParents();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Parent deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "Parent record not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
            }
        }
    }

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
    private void handleLevels() {
        loadView("/levels.fxml");
    }

    @FXML
    private void handleGrades() {
        loadView("/grades.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?");
        if (alert.showAndWait().get() == ButtonType.YES) {
            loadView("/login.fxml");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();

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
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}