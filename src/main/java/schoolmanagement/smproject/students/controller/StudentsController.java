package schoolmanagement.smproject.students.controller;

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

import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.students.repository.StudentRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class StudentsController {

    // === FXML ELEMENTS ===
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbGradeFilter;
    @FXML
    private ComboBox<String> cbStatusFilter;
    @FXML
    private TableView<Student> studentsTable;
    @FXML
    private TableColumn<Student, Integer> colId;
    @FXML
    private TableColumn<Student, String> colName;
    @FXML
    private TableColumn<Student, String> colEmail;
    @FXML
    private TableColumn<Student, String> colPhone;
    @FXML
    private TableColumn<Student, String> colGrade;
    @FXML
    private TableColumn<Student, String> colClassroom;
    @FXML
    private TableColumn<Student, String> colParent;
    @FXML
    private TableColumn<Student, String> colStatus;
    @FXML
    private TableColumn<Student, Void> colActions;
    @FXML
    private Label lblStudentCount;
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
    private List<Student> allStudents = List.of();
    private List<Student> filteredStudents = List.of();
    private int currentPage = 1;
    private final int pageSize = 15;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadStudents();
        setupRealtimeSearch();
        updatePaginationUI();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("gradeLevel"));

        colClassroom.setCellValueFactory(cell -> {
            Student s = cell.getValue();
            String classroom = s.getClassroom() != null ? s.getClassroom() : "N/A";
            return new ReadOnlyStringWrapper(classroom);
        });

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
                    Button btnBulletin = new Button("📋");
                    btnBulletin.getStyleClass().addAll("action-btn-small", "action-bulletin");
                    btnBulletin.setTooltip(new Tooltip("View Bulletin"));
                    btnBulletin.setOnAction(e -> handleBulletin(getTableView().getItems().get(getIndex())));

                    Button btnEdit = new Button("✏️");
                    btnEdit.getStyleClass().addAll("action-btn-small", "action-edit");
                    btnEdit.setTooltip(new Tooltip("Edit Student"));
                    btnEdit.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));

                    Button btnDelete = new Button("🗑️");
                    btnDelete.getStyleClass().addAll("action-btn-small", "action-delete");
                    btnDelete.setTooltip(new Tooltip("Delete Student"));
                    btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));

                    setGraphic(new javafx.scene.layout.HBox(6, btnBulletin, btnEdit, btnDelete));
                }
            }
        });
    }

    private void setupFilters() {
        cbGradeFilter.getItems().addAll("All Grades", "CE1", "CE2", "CE3", "CE4", "CE5", "CE6");
        cbGradeFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getItems().addAll("All Status", "Active", "Inactive", "Suspended");
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    private void setupRealtimeSearch() {
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbGradeFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

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
        lblStudentCount
                .setText("Showing " + filteredStudents.size() + " student" + (filteredStudents.size() != 1 ? "s" : ""));
    }

    private void updateTablePage() {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredStudents.size());

        if (start >= filteredStudents.size()) {
            studentsTable.getItems().clear();
        } else {
            studentsTable.getItems().setAll(filteredStudents.subList(start, end));
        }
    }

    private void updatePaginationUI() {
        int totalPages = Math.max(1, (int) Math.ceil(filteredStudents.size() / (double) pageSize));
        lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        btnPrevious.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
    }

    // ==========================================
    // ✅ EXPORT FUNCTIONS
    // ==========================================

    @FXML
    private void handleExportExcel() {
        if (filteredStudents.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No students to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Students to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser
                .setInitialFileName("Students_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx");

        File file = fileChooser.showSaveDialog(studentsTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToExcel(file, filteredStudents);
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
        if (filteredStudents.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No students to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Students to PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Students_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf");

        File file = fileChooser.showSaveDialog(studentsTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToPDF(file, filteredStudents);
                showAlert(Alert.AlertType.INFORMATION, "Success ✅",
                        "PDF exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export PDF:\n" + e.getMessage());
            }
        }
    }

    // 🔹 Excel Implementation
    private void exportToExcel(File file, List<Student> students) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Students");

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
        String[] headers = { "ID", "Full Name", "Email", "Phone", "Grade Level", "Classroom", "Primary Parent",
                "Status" };
        for (int i = 0; i < headers.length; i++) {
            // ✅ Use fully qualified name for POI Cell to avoid ambiguity
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create Data Rows
        int rowNum = 1;
        for (Student student : students) {
            Row row = sheet.createRow(rowNum++);

            // ✅ Use fully qualified name for POI Cell
            org.apache.poi.ss.usermodel.Cell cell0 = row.createCell(0);
            cell0.setCellValue(student.getId());
            org.apache.poi.ss.usermodel.Cell cell1 = row.createCell(1);
            cell1.setCellValue(student.getFullName());
            org.apache.poi.ss.usermodel.Cell cell2 = row.createCell(2);
            cell2.setCellValue(student.getEmail() != null ? student.getEmail() : "");
            org.apache.poi.ss.usermodel.Cell cell3 = row.createCell(3);
            cell3.setCellValue(student.getPhone() != null ? student.getPhone() : "");
            org.apache.poi.ss.usermodel.Cell cell4 = row.createCell(4);
            cell4.setCellValue(student.getGradeLevel());
            org.apache.poi.ss.usermodel.Cell cell5 = row.createCell(5);
            cell5.setCellValue(student.getClassroom() != null ? student.getClassroom() : "N/A");
            org.apache.poi.ss.usermodel.Cell cell6 = row.createCell(6);
            cell6.setCellValue(student.getPrimaryParent() != null ? student.getPrimaryParent().getFullName() : "N/A");
            org.apache.poi.ss.usermodel.Cell cell7 = row.createCell(7);
            cell7.setCellValue(student.getStatus() != null ? student.getStatus() : "Active");

            for (int i = 0; i < 8; i++)
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
    private void exportToPDF(File file, List<Student> students) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        PDFont fontRegular = PDType1Font.HELVETICA;

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        try {
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;

            // Title
            contentStream.beginText();
            contentStream.setFont(fontBold, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("STUDENTS LIST");
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
            contentStream.showText("Grade");
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText("Status");
            contentStream.endText();
            contentStream.setNonStrokingColor(0, 0, 0);
            yPosition -= 30;

            // Table Rows
            contentStream.setFont(fontRegular, 8);
            for (Student student : students) {
                if (yPosition < 100) { // Add new page if needed
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - margin;
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 10, yPosition - 12);
                contentStream.showText(student.getFullName());
                contentStream.newLineAtOffset(180, 0);
                contentStream.showText(student.getEmail() != null ? student.getEmail() : "");
                contentStream.newLineAtOffset(180, 0);
                contentStream.showText(student.getGradeLevel());
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText(student.getStatus() != null ? student.getStatus() : "Active");
                contentStream.endText();
                yPosition -= 20;
            }

            // Footer
            contentStream.beginText();
            contentStream.setFont(fontRegular, 8);
            contentStream.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            contentStream.newLineAtOffset(margin, 30);
            contentStream.showText("Total Students: " + students.size());
            contentStream.endText();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (contentStream != null) {
                contentStream.close();
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
        cbGradeFilter.getSelectionModel().selectFirst();
        cbStatusFilter.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAddStudent() {
        loadView("/studentsform.fxml");
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/studentsform.fxml"));
            Parent root = loader.load();

            CreateStudentController controller = loader.getController();
            controller.setStudentToEdit(student);

            Stage stage = (Stage) studentsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("School MS ➜ Edit Student - " + student.getFullName());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit form: " + e.getMessage());
        }
    }

    private void handleDelete(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Student");
        alert.setHeaderText("Remove " + student.getFullName() + "?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                new StudentRepository().deleteById(student.getId());
                loadStudents();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Student deleted successfully.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete student: " + e.getMessage());
            }
        }
    }

    private void handleBulletin(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/studentBulletin.fxml"));
            Parent root = loader.load();

            StudentBulletinController ctrl = loader.getController();
            ctrl.setStudent(student);

            Stage stage = (Stage) studentsTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("School MS ➜ Bulletin - " + student.getFullName());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load bulletin: " + e.getMessage());
        }
    }

    // === NAVIGATION ===
    @FXML
    private void handleDashboard() {
        loadView("/dashboard.fxml");
    }

    @FXML
    private void handleStudents() {
        /* Already here */ }

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