package schoolmanagement.smproject.reports.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import schoolmanagement.smproject.reports.entity.Report;
import schoolmanagement.smproject.reports.repository.ReportRepository;
import schoolmanagement.smproject.reports.service.ReportGeneratorService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsController {

    @FXML
    private TableView<Report> reportsTable;
    @FXML
    private TableColumn<Report, Integer> colId;
    @FXML
    private TableColumn<Report, String> colReportType;
    @FXML
    private TableColumn<Report, String> colReportName;
    @FXML
    private TableColumn<Report, String> colAcademicYear;
    @FXML
    private TableColumn<Report, LocalDate> colGeneratedDate;
    @FXML
    private TableColumn<Report, String> colStatus;
    @FXML
    private TableColumn<Report, Void> colActions;

    @FXML
    private ComboBox<String> cbReportType;
    @FXML
    private ComboBox<String> cbAcademicYear;
    @FXML
    private TextField txtSearch;

    @FXML
    private Label lblTotalReports, lblFinalized, lblDrafts, lblThisMonth, lblReportCount;

    // Sidebar Navigation
    @FXML
    private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnGrades, btnReports;

    private ReportGeneratorService reportService;
    private ReportRepository reportRepo;
    private List<Report> allReports;

    @FXML
    public void initialize() {
        reportService = new ReportGeneratorService();
        reportRepo = new ReportRepository();

        setupTableColumns();
        setupFilters();
        loadReports();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colReportType.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        colReportName.setCellValueFactory(new PropertyValueFactory<>("reportName"));
        colAcademicYear.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        colGeneratedDate.setCellValueFactory(new PropertyValueFactory<>("generatedDate"));

        // Status Badge Cell Factory
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<Report, String>() {
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

        colActions.setCellFactory(col -> new TableCell<Report, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Button btnView = new Button("👁️");
                    btnView.getStyleClass().addAll("action-btn-small", "action-view");
                    btnView.setOnAction(e -> handleViewReport(getTableView().getItems().get(getIndex())));

                    Button btnEdit = new Button("✏️");
                    btnEdit.getStyleClass().addAll("action-btn-small", "action-edit");
                    btnEdit.setOnAction(e -> handleEditReport(getTableView().getItems().get(getIndex())));

                    Button btnDelete = new Button("🗑️");
                    btnDelete.getStyleClass().addAll("action-btn-small", "action-delete");
                    btnDelete.setOnAction(e -> handleDeleteReport(getTableView().getItems().get(getIndex())));

                    Button btnPDF = new Button("📄");
                    btnPDF.getStyleClass().addAll("action-btn-small", "action-pdf");
                    btnPDF.setOnAction(e -> handleExportPDF(getTableView().getItems().get(getIndex())));

                    setGraphic(new javafx.scene.layout.HBox(6, btnView, btnEdit, btnDelete, btnPDF));
                }
            }
        });
    }

    private void setupFilters() {
        cbReportType.getItems().addAll("All Types", "Enrollment", "Attendance", "Parents", "Academic",
                "General Report");
        cbReportType.getSelectionModel().selectFirst();

        cbAcademicYear.getItems().addAll("All Years", "2025-2026", "2026-2027", "2027-2028");
        cbAcademicYear.getSelectionModel().selectFirst();

        // Real-time search listeners
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbReportType.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbAcademicYear.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadReports() {
        try {
            allReports = reportRepo.findAll();
            applyFilters();
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load reports: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (allReports == null)
            return;

        String searchTerm = txtSearch.getText().toLowerCase().trim();
        String typeFilter = cbReportType.getValue();
        String yearFilter = cbAcademicYear.getValue();

        List<Report> filtered = allReports.stream()
                .filter(r -> {
                    boolean matchesSearch = searchTerm.isEmpty() ||
                            (r.getReportName() != null && r.getReportName().toLowerCase().contains(searchTerm));
                    boolean matchesType = "All Types".equals(typeFilter) || typeFilter == null
                            || typeFilter.equals(r.getReportType());
                    boolean matchesYear = "All Years".equals(yearFilter) || yearFilter == null
                            || yearFilter.equals(r.getAcademicYear());
                    return matchesSearch && matchesType && matchesYear;
                })
                .collect(Collectors.toList());

        reportsTable.getItems().setAll(filtered);
        lblReportCount.setText("Showing " + filtered.size() + " reports");
    }

    private void updateStats() {
        if (allReports == null)
            return;

        lblTotalReports.setText(String.valueOf(allReports.size()));

        long finalized = allReports.stream()
                .filter(r -> "Final".equalsIgnoreCase(r.getStatus()) || "Generated".equalsIgnoreCase(r.getStatus()))
                .count();
        lblFinalized.setText(String.valueOf(finalized));

        long drafts = allReports.stream().filter(r -> "Draft".equalsIgnoreCase(r.getStatus())).count();
        lblDrafts.setText(String.valueOf(drafts));

        long thisMonth = allReports.stream().filter(r -> {
            if (r.getGeneratedDate() == null)
                return false;
            return r.getGeneratedDate().getMonth() == LocalDate.now().getMonth() &&
                    r.getGeneratedDate().getYear() == LocalDate.now().getYear();
        }).count();
        lblThisMonth.setText(String.valueOf(thisMonth));
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleClearFilters() {
        txtSearch.clear();
        cbReportType.getSelectionModel().selectFirst();
        cbAcademicYear.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleGenerateEnrollmentReport() {
        try {
            reportService.generateEnrollmentReport(cbAcademicYear.getValue(), "Fall", "Admin User");
            loadReports();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Enrollment report generated successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate report: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateAttendanceReport() {
        try {
            reportService.generateAttendanceReport(cbAcademicYear.getValue(), "Fall", "Admin User");
            loadReports();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Attendance report generated successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate report: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateParentReport() {
        try {
            reportService.generateParentReport("Admin User");
            loadReports();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Parent directory report generated successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate report: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateGradeReport() {
        try {
            reportService.generateGradeDistributionReport(cbAcademicYear.getValue(), "Admin User");
            loadReports();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Grade distribution report generated successfully!");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to generate report: " + e.getMessage());
        }
    }

    private void handleViewReport(Report report) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/viewReport.fxml"));
            Parent root = loader.load();

            ViewReportController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED); // Modern floating modal
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            controller.setStage(dialogStage);
            controller.setReport(report);

            dialogStage.setTitle("Report Details");
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);

            if (btnDashboard != null && btnDashboard.getScene() != null) {
                dialogStage.initOwner(btnDashboard.getScene().getWindow());
            }

            dialogStage.setResizable(true);
            dialogStage.setWidth(800);
            dialogStage.setHeight(650);
            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open report: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportPDF() {
        Report selectedReport = reportsTable.getSelectionModel().getSelectedItem();
        if (selectedReport == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a report to export.");
            return;
        }
        handleExportPDF(selectedReport);
    }

    private void handleExportPDF(Report report) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report to PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(report.getReportName() + "_" + LocalDate.now() + ".pdf");

        File file = fileChooser.showSaveDialog(reportsTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToPDF(file, report);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report exported to PDF successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export All Reports to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("All_Reports_" + LocalDate.now() + ".xlsx");

        File file = fileChooser.showSaveDialog(reportsTable.getScene().getWindow());
        if (file != null) {
            try {
                exportAllToExcel(file, allReports);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Reports exported to Excel successfully!");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export Excel: " + e.getMessage());
            }
        }
    }

    private void exportToPDF(File file, Report report) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        try {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(100, 750);
            contentStream.showText(cleanText(report.getReportName()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 11);
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText(cleanText("Report Type: " + report.getReportType()));
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(cleanText("Academic Year: " + report.getAcademicYear()));
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(cleanText("Generated: " + report.getGeneratedDate()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, 620);
            String summary = report.getSummaryData() == null ? "" : report.getSummaryData();
            for (String line : summary.split("\n")) {
                contentStream.showText(cleanText(line));
                contentStream.newLineAtOffset(0, -15);
            }
            contentStream.endText();
        } finally {
            contentStream.close();
        }
        document.save(file);
        document.close();
    }

    private void exportAllToExcel(File file, List<Report> reports) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reports");
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        String[] headers = { "ID", "Type", "Title", "Academic Year", "Report Date", "Status" };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        if (reports != null) {
            for (Report report : reports) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(nullToEmpty(report.getReportType()));
                row.createCell(2).setCellValue(nullToEmpty(report.getReportName()));
                row.createCell(3).setCellValue(nullToEmpty(report.getAcademicYear()));
                row.createCell(4)
                        .setCellValue(report.getGeneratedDate() == null ? "" : report.getGeneratedDate().toString());
                row.createCell(5).setCellValue(nullToEmpty(report.getStatus()));
            }
        }
        for (int i = 0; i < headers.length; i++)
            sheet.autoSizeColumn(i);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    @FXML
    private void handleGenerateReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/generateReport.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED); // Modern floating modal
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Generate New Report");

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);

            if (btnDashboard != null && btnDashboard.getScene() != null) {
                dialogStage.initOwner(btnDashboard.getScene().getWindow());
            }

            dialogStage.setResizable(false);
            dialogStage.showAndWait();
            loadReports();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open report generator: " + e.getMessage());
        }
    }

    private void handleEditReport(Report report) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/generateReport.fxml"));
            Parent root = loader.load();

            GenerateReportController controller = loader.getController();
            controller.setReportForEdit(report);

            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Edit Report");

            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            dialogStage.setScene(scene);

            if (btnDashboard != null && btnDashboard.getScene() != null) {
                dialogStage.initOwner(btnDashboard.getScene().getWindow());
            }

            dialogStage.showAndWait();
            loadReports();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open edit form: " + e.getMessage());
        }
    }

    private void handleDeleteReport(Report report) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Report");
        alert.setHeaderText("Delete " + report.getReportName() + "?");
        alert.setContentText("This report will be permanently removed.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean deleted = reportRepo.deleteById(report.getId());
                if (deleted) {
                    loadReports();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Report deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.WARNING, "Not Found", "Report not found.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete report: " + e.getMessage());
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
        loadView("/courses.fxml");
    }

    @FXML
    private void handleGrades() {
        loadView("/grades.fxml");
    }

    @FXML
    private void handleReports() {
        /* Already here */ }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?");
        if (alert.showAndWait().get() == ButtonType.YES)
            loadView("/login.fxml");
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load: " + fxmlPath);
        }
    }

    private String cleanText(String text) {
        return text == null ? "" : text.replaceAll("[^\\x00-\\x7F]", "");
    }

    private String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}