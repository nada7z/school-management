package schoolmanagement.smproject.attendance.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

// Excel & PDF Imports
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;

import schoolmanagement.smproject.attendance.entity.Attendance;
import schoolmanagement.smproject.attendance.repository.AttendanceRepository;
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.students.repository.StudentRepository;
import schoolmanagement.smproject.teachers.entity.Teacher;
import schoolmanagement.smproject.teachers.repository.TeacherRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceController {

    // === FXML ELEMENTS ===
    @FXML
    private DatePicker dpDate;
    @FXML
    private ComboBox<String> cbType;
    @FXML
    private TextField txtSearch;
    @FXML
    private ComboBox<String> cbStatusFilter;

    @FXML
    private TableView<AttendanceRecord> attendanceTable;
    @FXML
    private TableColumn<AttendanceRecord, Integer> colId;
    @FXML
    private TableColumn<AttendanceRecord, String> colName;
    @FXML
    private TableColumn<AttendanceRecord, String> colClassOrSubject;
    @FXML
    private TableColumn<AttendanceRecord, String> colStatus;
    @FXML
    private TableColumn<AttendanceRecord, String> colRemarks;
    @FXML
    private TableColumn<AttendanceRecord, Void> colActions;

    @FXML
    private Label lblPresentCount, lblAbsentCount, lblLateCount, lblAttendanceRate;
    @FXML
    private Label lblRecordCount, lblPageInfo;
    @FXML
    private Button btnPrevious, btnNext;

    // Sidebar Navigation
    @FXML
    private Button btnDashboard, btnStudents, btnTeachers, btnAttendance, btnCourses, btnGrades;

    // === STATE VARIABLES ===
    private final AttendanceRepository attendanceRepo = new AttendanceRepository();
    private final StudentRepository studentRepo = new StudentRepository();
    private final TeacherRepository teacherRepo = new TeacherRepository();

    private List<AttendanceRecord> allRecords = new ArrayList<>();
    private List<AttendanceRecord> filteredRecords = new ArrayList<>();
    private int currentPage = 1;
    private final int pageSize = 15;

    @FXML
    public void initialize() {
        // Initialize Filters
        dpDate.setValue(LocalDate.now());
        cbType.getItems().addAll("Students", "Teachers");
        cbType.getSelectionModel().selectFirst();

        cbStatusFilter.getItems().addAll("All Status", "PRESENT", "ABSENT", "LATE", "EXCUSED");
        cbStatusFilter.getSelectionModel().selectFirst();

        setupTableColumns();
        setupListeners();
        loadAttendance();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("personId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colClassOrSubject.setCellValueFactory(new PropertyValueFactory<>("classOrSubject"));

        // Status Column with Inline ComboBox
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<AttendanceRecord, String>() {
            private final ComboBox<String> cb = new ComboBox<>();
            {
                cb.getItems().addAll("PRESENT", "ABSENT", "LATE", "EXCUSED");
                cb.getStyleClass().add("combo-box-small");
                cb.setMaxWidth(Double.MAX_VALUE);
                cb.setOnAction(e -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        AttendanceRecord record = getTableView().getItems().get(index);
                        record.setStatus(cb.getValue());
                        updateStats(); // Live update stats
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    cb.setValue(item);
                    setGraphic(cb);
                }
            }
        });

        // Remarks Column with Inline TextField
        colRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));
        colRemarks.setCellFactory(col -> new TableCell<AttendanceRecord, String>() {
            private final TextField tf = new TextField();
            {
                tf.setPromptText("Add remarks...");
                tf.getStyleClass().add("text-field");
                tf.textProperty().addListener((obs, oldVal, newVal) -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        AttendanceRecord record = getTableView().getItems().get(index);
                        record.setRemarks(newVal);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    tf.setText(item != null ? item : "");
                    setGraphic(tf);
                }
            }
        });

        // Actions Column (Reset Button)
        colActions.setCellFactory(col -> new TableCell<AttendanceRecord, Void>() {
            private final Button btnReset = new Button("🔄");
            {
                btnReset.getStyleClass().addAll("action-btn-small", "action-edit");
                btnReset.setTooltip(new Tooltip("Reset to Present"));
                btnReset.setOnAction(e -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    record.setStatus("PRESENT");
                    record.setRemarks("");
                    attendanceTable.refresh();
                    updateStats();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnReset);
                }
            }
        });
    }

    private void setupListeners() {
        // Reload data when Date or Type changes
        dpDate.valueProperty().addListener((obs, oldVal, newVal) -> loadAttendance());
        cbType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadAttendance());

        // Filter data when Search or Status changes
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbStatusFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadAttendance() {
        LocalDate date = dpDate.getValue();
        if (date == null)
            date = LocalDate.now();

        String type = cbType.getValue();
        if (type == null)
            type = "Students";

        boolean isStudent = type.equals("Students");
        String personTypeDb = isStudent ? "STUDENT" : "TEACHER";

        try {
            List<Attendance> existingAttendance = attendanceRepo.findByDateAndType(date, personTypeDb);
            List<AttendanceRecord> records = new ArrayList<>();

            if (isStudent) {
                List<Student> students = studentRepo.findAll();
                for (Student s : students) {
                    Attendance existing = existingAttendance.stream()
                            .filter(a -> a.getPersonId() == s.getId())
                            .findFirst().orElse(null);

                    AttendanceRecord record = new AttendanceRecord();
                    record.setPersonId(s.getId());
                    record.setPersonType(personTypeDb);
                    record.setName(s.getFirstName() + " " + s.getLastName());
                    record.setClassOrSubject(
                            s.getGradeLevel() + " - " + (s.getClassroom() != null ? s.getClassroom() : "N/A"));
                    record.setStatus(existing != null ? existing.getStatus() : "PRESENT");
                    record.setRemarks(existing != null ? existing.getRemarks() : "");
                    records.add(record);
                }
            } else {
                List<Teacher> teachers = teacherRepo.findAll();
                for (Teacher t : teachers) {
                    Attendance existing = existingAttendance.stream()
                            .filter(a -> a.getPersonId() == t.getId())
                            .findFirst().orElse(null);

                    AttendanceRecord record = new AttendanceRecord();
                    record.setPersonId(t.getId());
                    record.setPersonType(personTypeDb);
                    record.setName(t.getFirstName() + " " + t.getLastName());
                    record.setClassOrSubject(
                            t.getSubjectSpecialization() != null ? t.getSubjectSpecialization() : "N/A");
                    record.setStatus(existing != null ? existing.getStatus() : "PRESENT");
                    record.setRemarks(existing != null ? existing.getRemarks() : "");
                    records.add(record);
                }
            }

            allRecords = records;
            applyFilters();
            updateStats();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load attendance: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String searchTerm = txtSearch.getText().toLowerCase().trim();
        String statusFilter = cbStatusFilter.getValue();

        filteredRecords = allRecords.stream()
                .filter(r -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            r.getName().toLowerCase().contains(searchTerm) ||
                            String.valueOf(r.getPersonId()).contains(searchTerm);
                    boolean matchesStatus = "All Status".equals(statusFilter) ||
                            statusFilter.equalsIgnoreCase(r.getStatus());
                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        currentPage = 1;
        updateTablePage();
        updatePaginationUI();
        lblRecordCount.setText("Showing " + filteredRecords.size() + " records");
    }

    private void updateStats() {
        long present = allRecords.stream().filter(r -> "PRESENT".equals(r.getStatus())).count();
        long absent = allRecords.stream().filter(r -> "ABSENT".equals(r.getStatus())).count();
        long late = allRecords.stream().filter(r -> "LATE".equals(r.getStatus())).count();

        lblPresentCount.setText(String.valueOf(present));
        lblAbsentCount.setText(String.valueOf(absent));
        lblLateCount.setText(String.valueOf(late));

        if (!allRecords.isEmpty()) {
            double rate = (present + late) * 100.0 / allRecords.size();
            lblAttendanceRate.setText(String.format("%.1f%%", rate));
        } else {
            lblAttendanceRate.setText("0%");
        }
    }

    private void updateTablePage() {
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredRecords.size());
        if (start >= filteredRecords.size()) {
            attendanceTable.getItems().clear();
        } else {
            attendanceTable.getItems().setAll(filteredRecords.subList(start, end));
        }
    }

    private void updatePaginationUI() {
        int totalPages = Math.max(1, (int) Math.ceil(filteredRecords.size() / (double) pageSize));
        lblPageInfo.setText("Page " + currentPage + " of " + totalPages);
        btnPrevious.setDisable(currentPage <= 1);
        btnNext.setDisable(currentPage >= totalPages);
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
        cbStatusFilter.getSelectionModel().selectFirst();
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
        int totalPages = (int) Math.ceil(filteredRecords.size() / (double) pageSize);
        if (currentPage < totalPages) {
            currentPage++;
            updateTablePage();
            updatePaginationUI();
        }
    }

    @FXML
    private void handleSaveAll() {
        LocalDate date = dpDate.getValue();
        if (date == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select a date.");
            return;
        }

        try {
            int savedCount = 0;
            for (AttendanceRecord record : allRecords) { // Save ALL records, not just filtered
                Attendance attendance = new Attendance();
                attendance.setPersonType(record.getPersonType());
                attendance.setPersonId(record.getPersonId());
                attendance.setPersonName(record.getName());
                attendance.setClassName(record.getClassOrSubject());
                attendance.setDate(date);
                attendance.setStatus(record.getStatus());
                attendance.setRemarks(record.getRemarks());

                attendanceRepo.save(attendance);
                savedCount++;
            }
            showAlert(Alert.AlertType.INFORMATION, "Success ✅",
                    "Attendance saved successfully for " + savedCount + " records.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save attendance: " + e.getMessage());
        }
    }

    // ==========================================
    // EXPORT FUNCTIONS
    // ==========================================

    @FXML
    private void handleExportExcel() {
        if (filteredRecords.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No attendance records to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser
                .setInitialFileName("Attendance_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx");

        File file = fileChooser.showSaveDialog(attendanceTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToExcel(file, filteredRecords);
                showAlert(Alert.AlertType.INFORMATION, "Success ✅",
                        "Excel exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export Excel:\n" + e.getMessage());
            }
        }
    }

    private void exportToExcel(File file, List<AttendanceRecord> records) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Attendance");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = { "ID", "Type", "Full Name", "Class/Subject", "Date", "Status", "Remarks" };
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (AttendanceRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getPersonId());
            row.createCell(1).setCellValue(record.getPersonType());
            row.createCell(2).setCellValue(record.getName());
            row.createCell(3).setCellValue(record.getClassOrSubject());
            row.createCell(4).setCellValue(dpDate.getValue().toString());
            row.createCell(5).setCellValue(record.getStatus());
            row.createCell(6).setCellValue(record.getRemarks() != null ? record.getRemarks() : "");
        }

        for (int i = 0; i < headers.length; i++)
            sheet.autoSizeColumn(i);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    @FXML
    private void handleExportPDF() {
        if (filteredRecords.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No attendance records to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance to PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser
                .setInitialFileName("Attendance_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf");

        File file = fileChooser.showSaveDialog(attendanceTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToPDF(file, filteredRecords);
                showAlert(Alert.AlertType.INFORMATION, "Success ✅",
                        "PDF exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export PDF:\n" + e.getMessage());
            }
        }
    }

    private void exportToPDF(File file, List<AttendanceRecord> records) throws IOException {
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

            contentStream.beginText();
            contentStream.setFont(fontBold, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("ATTENDANCE RECORD");
            contentStream.endText();
            yPosition -= 30;

            contentStream.setFont(fontRegular, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Date: " + dpDate.getValue().toString() + " | Type: " + cbType.getValue());
            contentStream.endText();
            yPosition -= 40;

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
            contentStream.showText("Class/Subject");
            contentStream.newLineAtOffset(120, 0);
            contentStream.showText("Status");
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText("Remarks");
            contentStream.endText();
            contentStream.setNonStrokingColor(0, 0, 0);
            yPosition -= 30;

            contentStream.setFont(fontRegular, 8);
            for (AttendanceRecord record : records) {
                if (yPosition < 100) {
                    contentStream.close();
                    page = new PDPage();
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - margin;
                }

                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 10, yPosition - 12);
                contentStream.showText(record.getName());
                contentStream.newLineAtOffset(180, 0);
                contentStream.showText(record.getClassOrSubject());
                contentStream.newLineAtOffset(120, 0);
                contentStream.showText(record.getStatus());
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText(record.getRemarks() != null ? record.getRemarks() : "");
                contentStream.endText();
                yPosition -= 20;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (contentStream != null)
                contentStream.close();
            document.save(file);
            document.close();
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
    private void handleAttendance() {
        /* Already here */ }

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
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?");
        if (alert.showAndWait().get() == ButtonType.YES) {
            loadView("/login.fxml");
        }
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) btnDashboard.getScene().getWindow();

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

    // === INNER CLASS FOR TABLE VIEW ===
    public static class AttendanceRecord {
        private int personId;
        private String personType;
        private String name;
        private String classOrSubject;
        private String status;
        private String remarks;

        public int getPersonId() {
            return personId;
        }

        public void setPersonId(int personId) {
            this.personId = personId;
        }

        public String getPersonType() {
            return personType;
        }

        public void setPersonType(String personType) {
            this.personType = personType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getClassOrSubject() {
            return classOrSubject;
        }

        public void setClassOrSubject(String classOrSubject) {
            this.classOrSubject = classOrSubject;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }
    }
}