package schoolmanagement.smproject.teachers.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// ✅ Excel Imports (Specific classes to avoid 'Cell' conflict)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// ✅ PDF Imports
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;

import schoolmanagement.smproject.teachers.entity.Teacher;
import schoolmanagement.smproject.teachers.repository.TeacherRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class TeachersController {

    // === FXML ELEMENTS ===
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbSubjectFilter;
    @FXML
    private ComboBox<String> cbStatusFilter;
    @FXML
    private TableView<Teacher> teachersTable;
    @FXML
    private TableColumn<Teacher, Integer> colId;
    @FXML
    private TableColumn<Teacher, String> colName;
    @FXML
    private TableColumn<Teacher, String> colEmail;
    @FXML
    private TableColumn<Teacher, String> colPhone;
    @FXML
    private TableColumn<Teacher, String> colSubject;
    @FXML
    private TableColumn<Teacher, String> colQualification;
    @FXML
    private TableColumn<Teacher, String> colStatus;
    @FXML
    private TableColumn<Teacher, Void> colActions;
    @FXML
    private Label lblTeacherCount;
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
    private List<Teacher> allTeachers = List.of();
    private List<Teacher> filteredTeachers = List.of();
    private int currentPage = 1;
    private final int pageSize = 15;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadTeachers();
        setupRealtimeSearch();
        updatePaginationUI();
    }

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
                    btnEdit.setTooltip(new Tooltip("Edit"));
                    btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));

                    Button btnDelete = new Button("🗑️");
                    btnDelete.getStyleClass().addAll("action-btn-small", "action-delete");
                    btnDelete.setTooltip(new Tooltip("Delete"));
                    btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                    setGraphic(new javafx.scene.layout.HBox(6, btnEdit, btnDelete));
                }
            }
        });
    }

    private void setupFilters() {
        cbSubjectFilter.getItems().addAll("All Subjects", "Mathematics", "French", "Science", "History", "English",
                "Other");
        cbSubjectFilter.getSelectionModel().selectFirst();

        cbStatusFilter.getItems().addAll("All Status", "Active", "Inactive", "On Leave");
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    private void setupRealtimeSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbSubjectFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

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
                            (t.getSubjectSpecialization() != null
                                    && t.getSubjectSpecialization().toLowerCase().contains(searchTerm))
                            ||
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
        lblTeacherCount
                .setText("Showing " + filteredTeachers.size() + " teacher" + (filteredTeachers.size() != 1 ? "s" : ""));
    }

    private void updateTablePage() {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredTeachers.size());

        if (start >= filteredTeachers.size()) {
            teachersTable.getItems().clear();
        } else {
            teachersTable.getItems().setAll(filteredTeachers.subList(start, end));
        }
    }

    private void updatePaginationUI() {
        int totalPages = Math.max(1, (int) Math.ceil(filteredTeachers.size() / (double) pageSize));
        lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        btnPrevious.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    // ==========================================
    // ✅ EXPORT FUNCTIONS
    // ==========================================

    @FXML
    private void handleExportExcel() {
        if (filteredTeachers.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No teachers to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Teachers to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser
                .setInitialFileName("Teachers_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx");

        File file = fileChooser.showSaveDialog(teachersTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToExcel(file, filteredTeachers);
                showAlert(Alert.AlertType.INFORMATION, "Success ✅",
                        "Excel exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export Excel:\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportPDF() {
        if (filteredTeachers.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No teachers to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Teachers to PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Teachers_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf");

        File file = fileChooser.showSaveDialog(teachersTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToPDF(file, filteredTeachers);
                showAlert(Alert.AlertType.INFORMATION, "Success ✅",
                        "PDF exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export PDF:\n" + e.getMessage());
            }
        }
    }

    // 🔹 Excel Implementation
    private void exportToExcel(File file, List<Teacher> teachers) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Teachers");

        // Header Style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        // Data Style
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);

        // Create Header Row
        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "Full Name", "Email", "Phone", "Subject", "Qualification", "Status" };
        for (int i = 0; i < headers.length; i++) {
            // ✅ Use fully qualified name for POI Cell to avoid ambiguity
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create Data Rows
        int rowNum = 1;
        for (Teacher teacher : teachers) {
            Row row = sheet.createRow(rowNum++);

            // ✅ Use fully qualified name for POI Cell
            org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
            cell0.setCellValue(teacher.getId());
            org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
            cell1.setCellValue(teacher.getFullName());
            org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
            cell2.setCellValue(teacher.getEmail() != null ? teacher.getEmail() : "");
            org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
            cell3.setCellValue(teacher.getPhone() != null ? teacher.getPhone() : "");
            org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
            cell4.setCellValue(teacher.getSubjectSpecialization() != null ? teacher.getSubjectSpecialization() : "");
            org.apache.poi.ss.usermodel.Cell cell5 = row.createCell(5);
            cell5.setCellValue(teacher.getQualification() != null ? teacher.getQualification() : "");
            org.apache.poi.ss.usermodel.Cell cell6 = row.createCell(6);
            cell6.setCellValue(teacher.getStatus() != null ? teacher.getStatus() : "Active");

            for (int i = 0; i < 7; i++)
                row.getCell(i).setCellStyle(cellStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++)
            sheet.autoSizeColumn(i);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    // 🔹 PDF Implementation
    private void exportToPDF(File file, List<Teacher> teachers) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        PDFont fontRegular = PDType1Font.HELVETICA;

        PDPageContentStream contentStream = null;
        try {
            contentStream = new PDPageContentStream(document, page);
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;

            // Title
            contentStream.beginText();
            contentStream.setFont(fontBold, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("TEACHERS LIST");
            contentStream.endText();
            yPosition -= 30;

            // School Name & Date
            contentStream.setFont(fontRegular, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("School Management System");
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Generated: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            contentStream.endText();
            yPosition -= 40;

            // Table Header
            contentStream.setStrokingColor(0.2f, 0.4f, 0.6f);
            contentStream.setLineWidth(1.5f);
            contentStream.addRect(margin, yPosition - 25, tableWidth, 25);
            contentStream.stroke();

            contentStream.beginText();
            contentStream.setFont(fontBold, 9);
            contentStream.setNonStrokingColor(1, 1, 1);
            contentStream.newLineAtOffset(margin + 10, yPosition - 15);
            contentStream.showText("Name");
            contentStream.newLineAtOffset(180, 0);
            contentStream.showText("Email");
            contentStream.newLineAtOffset(180, 0);
            contentStream.showText("Subject");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText("Status");
            contentStream.endText();
            contentStream.setNonStrokingColor(0, 0, 0);
            yPosition -= 30;

            // Table Rows
            contentStream.setFont(fontRegular, 8);
            for (Teacher teacher : teachers) {
                if (yPosition < 100) { // Add new page if needed
                    if (contentStream != null)
                        contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - margin;
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 10, yPosition - 12);
                contentStream.showText(teacher.getFullName());
                contentStream.newLineAtOffset(180, 0);
                contentStream.showText(teacher.getEmail() != null ? teacher.getEmail() : "");
                contentStream.newLineAtOffset(180, 0);
                contentStream
                        .showText(teacher.getSubjectSpecialization() != null ? teacher.getSubjectSpecialization() : "");
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText(teacher.getStatus() != null ? teacher.getStatus() : "Active");
                contentStream.endText();
                yPosition -= 20;
            }

            // Footer
            contentStream.beginText();
            contentStream.setFont(fontRegular, 8);
            contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            contentStream.newLineAtOffset(margin, 30);
            contentStream.showText("Total Teachers: " + teachers.size());
            contentStream.endText();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (contentStream != null)
                    contentStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            document.save(file);
            document.close();
        }
    }

    // ==========================================
    // EVENT HANDLERS
    // ==========================================

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        txtSearch.clear();
        cbSubjectFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAddTeacher() {
        loadView("/createTeacher.fxml");
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
        int totalPages = (int) Math.ceil(filteredTeachers.size() / (double) pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            updateTablePage();
            updatePaginationUI();
        }
    }

    private void handleEdit(Teacher teacher) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/teachersform.fxml"));
            Parent root = loader.load();

            CreateTeacherController controller = loader.getController();
            controller.setTeacherForEdit(teacher);

            Stage stage = (Stage) teachersTable.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open edit form: " + e.getMessage());
        }
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
        /* Already here */ }

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
}