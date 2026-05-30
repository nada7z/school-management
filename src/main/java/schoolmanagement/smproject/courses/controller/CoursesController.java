package schoolmanagement.smproject.courses.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
// ✅ Use YOUR existing entity and repository packages
import schoolmanagement.smproject.courses.entity.Course;
import schoolmanagement.smproject.courses.repository.CourseRepository;

import java.util.List;
import java.util.stream.Collectors;

public class CoursesController {

    // === FXML ELEMENTS ===
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbLevelFilter;
    @FXML
    private ComboBox<String> cbStatusFilter;
    @FXML
    private TableView<Course> coursesTable;
    @FXML
    private TableColumn<Course, Integer> colId;
    @FXML
    private TableColumn<Course, String> colCode;
    @FXML
    private TableColumn<Course, String> colName;
    @FXML
    private TableColumn<Course, String> colLevel;
    @FXML
    private TableColumn<Course, String> colTeacher;
    @FXML
    private TableColumn<Course, Integer> colHours;
    @FXML
    private TableColumn<Course, Integer> colCapacity;
    @FXML
    private TableColumn<Course, String> colStatus;
    @FXML
    private TableColumn<Course, Void> colActions;
    @FXML
    private Label lblCourseCount;
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
    private List<Course> allCourses = List.of();
    private List<Course> filteredCourses = List.of();
    private int currentPage = 1;
    private final int pageSize = 15;

    /**
     * Called after FXML is loaded. Sets up UI, columns, and loads initial data.
     */
    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadCourses();
        setupRealtimeSearch();
        updatePaginationUI();
    }

    /**
     * Configures TableView columns and cell factories.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLevel.setCellValueFactory(new PropertyValueFactory<>("levelName"));
        colTeacher.setCellValueFactory(new PropertyValueFactory<>("teacherName"));
        colHours.setCellValueFactory(new PropertyValueFactory<>("hoursPerWeek"));
        colCapacity.setCellValueFactory(new PropertyValueFactory<>("maxCapacity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Status Badge Cell Factory
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status.toUpperCase());
                    badge.getStyleClass().addAll("status-badge", "status-" + status.toLowerCase());
                    setText(null);
                    setGraphic(badge);
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
        cbLevelFilter.getItems().addAll("All Levels", "CE1", "CE2", "CE3", "CE4", "CE5", "CE6");
        cbLevelFilter.getSelectionModel().selectFirst();

        cbStatusFilter.getItems().addAll("All Status", "Active", "Inactive", "Archived");
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    /**
     * Enables real-time filtering as user types.
     */
    private void setupRealtimeSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbLevelFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    /**
     * Fetches courses from database using YOUR repository.
     */
    private void loadCourses() {
        try {
            CourseRepository repo = new CourseRepository();
            allCourses = repo.findAll();
            applyFilters();
        } catch (Exception e) {
            e.printStackTrace();
            allCourses = List.of();
            filteredCourses = List.of();
            coursesTable.getItems().clear();
            lblCourseCount.setText("Showing 0 courses (Database unavailable)");
        }
    }

    /**
     * Applies search and filter criteria.
     */
    private void applyFilters() {
        String searchTerm = txtSearch.getText().toLowerCase().trim();
        String levelFilter = cbLevelFilter.getValue();
        String statusFilter = cbStatusFilter.getValue();

        filteredCourses = allCourses.stream()
                .filter(c -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            c.getCourseCode().toLowerCase().contains(searchTerm) ||
                            c.getName().toLowerCase().contains(searchTerm) ||
                            (c.getTeacherName() != null && c.getTeacherName().toLowerCase().contains(searchTerm)) ||
                            String.valueOf(c.getId()).contains(searchTerm);

                    boolean matchesLevel = "All Levels".equals(levelFilter) ||
                            levelFilter.equals(c.getLevelName());
                    boolean matchesStatus = "All Status".equals(statusFilter) ||
                            statusFilter.equals(c.getStatus());

                    return matchesSearch && matchesLevel && matchesStatus;
                })
                .collect(Collectors.toList());

        currentPage = 1;
        updateTablePage();
        updatePaginationUI();
        lblCourseCount
                .setText("Showing " + filteredCourses.size() + " course" + (filteredCourses.size() != 1 ? "s" : ""));
    }

    /**
     * Updates table with current page data.
     */
    private void updateTablePage() {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredCourses.size());

        if (start >= filteredCourses.size()) {
            coursesTable.getItems().clear();
        } else {
            coursesTable.getItems().setAll(filteredCourses.subList(start, end));
        }
    }

    /**
     * Updates pagination UI state.
     */
    private void updatePaginationUI() {
        int totalPages = Math.max(1, (int) Math.ceil(filteredCourses.size() / (double) pageSize));
        lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        btnPrevious.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    // === EVENT HANDLERS ===

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        txtSearch.clear();
        cbLevelFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAddCourse() {
        loadView("/courseform.fxml");
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
        int totalPages = (int) Math.ceil(filteredCourses.size() / (double) pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            updateTablePage();
            updatePaginationUI();
        }
    }

    private void handleEdit(Course course) {
        showAlert(Alert.AlertType.INFORMATION, "Edit Course",
                "Edit form for " + course.getName() + " coming soon!\n\nCode: " + course.getCourseCode());
    }

    private void handleDelete(Course course) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Course");
        alert.setHeaderText("Remove " + course.getName() + "?");
        alert.setContentText("This will NOT delete enrolled students, but will remove course record.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                CourseRepository repo = new CourseRepository();
                boolean deleted = repo.deleteById(course.getId());

                if (deleted) {
                    loadCourses();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "Course record not found.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete: " + e.getMessage());
            }
        }
    }

    // === NAVIGATION ===

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
        /* Already here */ }

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

    // === HELPERS ===

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
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    @FXML
    private void handleExportExcel() {
        try {
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Courses");

            // Create header style
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Headers
            String[] headers = { "ID", "Code", "Course Name", "Level", "Teacher", "Hours", "Capacity", "Status" };
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Course course : filteredCourses) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(course.getId());
                row.createCell(1).setCellValue(course.getCourseCode());
                row.createCell(2).setCellValue(course.getName());
                row.createCell(3).setCellValue(course.getLevelName());
                row.createCell(4).setCellValue(course.getTeacherName() != null ? course.getTeacherName() : "N/A");
                row.createCell(5).setCellValue(course.getHoursPerWeek());
                row.createCell(6).setCellValue(course.getMaxCapacity());
                row.createCell(7).setCellValue(course.getStatus());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save dialog
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Courses to Excel");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            fileChooser.setInitialFileName("courses_" + java.time.LocalDate.now() + ".xlsx");

            java.io.File file = fileChooser.showSaveDialog(coursesTable.getScene().getWindow());
            if (file != null) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Exported " + filteredCourses.size() + " courses to Excel!");
            }
            workbook.close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to export Excel: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Courses to PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            fileChooser.setInitialFileName("courses_" + LocalDate.now() + ".pdf");

            java.io.File file = fileChooser.showSaveDialog(coursesTable.getScene().getWindow());
            if (file == null)
                return;

            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Courses Report");
            contentStream.endText();

            // Subtitle
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, 730);
            contentStream.showText("Generated: " + java.time.LocalDateTime.now());
            contentStream.endText();

            // Table headers
            String[] headers = { "ID", "Code", "Course Name", "Level", "Teacher", "Hours", "Capacity", "Status" };
            float[] columnWidths = { 40, 70, 150, 70, 130, 40, 60, 60 };
            float margin = 50;
            float yStart = 690;
            float rowHeight = 15;
            float headerY = yStart;

            // Draw header background (gray rectangle)
            contentStream.setNonStrokingColor(java.awt.Color.GRAY);
            contentStream.addRect(margin, headerY - rowHeight, 490, rowHeight);
            contentStream.fill();

            // Draw header text (white)
            contentStream.setNonStrokingColor(java.awt.Color.WHITE);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
            float xPosition = margin + 5;
            float headerYText = headerY - 10;
            for (int i = 0; i < headers.length; i++) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
                contentStream.newLineAtOffset(xPosition, headerYText);
                contentStream.showText(headers[i]);
                contentStream.endText();
                xPosition += columnWidths[i];
            }

            // Draw table data
            contentStream.setNonStrokingColor(java.awt.Color.BLACK);
            float currentY = headerY - rowHeight - 5;

            for (int i = 0; i < filteredCourses.size(); i++) {
                Course course = filteredCourses.get(i);

                // Check if we need a new page
                if (currentY < 50) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    currentY = 750;
                }

                xPosition = margin + 5;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xPosition, currentY);
                contentStream.showText(String.valueOf(course.getId()));
                contentStream.endText();

                xPosition += columnWidths[0];
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xPosition, currentY);
                contentStream.showText(course.getCourseCode());
                contentStream.endText();

                xPosition += columnWidths[1];
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xPosition, currentY);
                contentStream.showText(course.getName());
                contentStream.endText();

                xPosition += columnWidths[2];
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xPosition, currentY);
                contentStream.showText(course.getLevelName());
                contentStream.endText();

                xPosition += columnWidths[3];
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xPosition, currentY);
                contentStream.showText(course.getTeacherName() != null ? course.getTeacherName() : "N/A");
                contentStream.endText();

                xPosition += columnWidths[4];
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xPosition, currentY);
                contentStream.showText(String.valueOf(course.getHoursPerWeek()));
                contentStream.endText();

                xPosition += columnWidths[5];
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xPosition, currentY);
                contentStream.showText(String.valueOf(course.getMaxCapacity()));
                contentStream.endText();

                xPosition += columnWidths[6];
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 8);
                contentStream.newLineAtOffset(xPosition, currentY);
                contentStream.showText(course.getStatus());
                contentStream.endText();

                currentY -= rowHeight;
            }

            contentStream.close();
            document.save(file);
            document.close();

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Exported " + filteredCourses.size() + " courses to PDF!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Export Error",
                    "Failed to export PDF: " + e.getMessage());
        }
    }
}