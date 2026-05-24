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
    private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnGrades;

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
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colActions.setCellFactory(col -> new TableCell<Report, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                Button btnView = new Button("👁️");
                btnView.getStyleClass().addAll("action-btn-small", "action-view");
                btnView.setOnAction(e -> handleViewReport(getTableView().getItems().get(getIndex())));

                Button btnPDF = new Button("📄");
                btnPDF.getStyleClass().addAll("action-btn-small", "action-pdf");
                btnPDF.setOnAction(e -> handleExportPDF(getTableView().getItems().get(getIndex())));

                setGraphic(new javafx.scene.layout.HBox(6, btnView, btnPDF));
            }
        });
    }

    private void setupFilters() {
        cbReportType.getItems().addAll("All", "Enrollment", "Attendance", "Parents", "Academic");
        cbReportType.getSelectionModel().selectFirst();

        cbAcademicYear.getItems().addAll("2025-2026", "2026-2027", "2027-2028");
        cbAcademicYear.getSelectionModel().selectFirst();
    }

    private void loadReports() {
        try {
            allReports = reportRepo.findAll();
            reportsTable.getItems().setAll(allReports);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load reports: " + e.getMessage());
        }
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
            controller.setStage(dialogStage);
            controller.setReport(report);

            dialogStage.setTitle("Report Details");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.APPLICATION_MODAL);

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
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(cleanText("Generated By: " + report.getGeneratedBy()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, 620);

            String summary = report.getSummaryData() == null ? "" : report.getSummaryData();
            String[] lines = summary.split("\n");

            for (String line : lines) {
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

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

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
            dialogStage.setTitle("Generate New Report");
            dialogStage.setScene(new Scene(root));
            dialogStage.initModality(Modality.APPLICATION_MODAL);

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
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Confirm Logout");
        alert.setContentText("Are you sure you want to logout?");
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            loadView("/login.fxml");
        }
    }

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

    private String cleanText(String text) {
        if (text == null) {
            return "";
        }

        return text.replaceAll("[^\\x00-\\x7F]", "");
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