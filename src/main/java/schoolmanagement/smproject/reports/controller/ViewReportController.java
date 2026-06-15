package schoolmanagement.smproject.reports.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import schoolmanagement.smproject.reports.entity.Report;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ViewReportController {
    @FXML
    private Label lblReportTitle, lblReportType, lblAcademicYear, lblGeneratedDate, lblStatus;
    @FXML
    private TextArea txtReportContent;

    private Report currentReport;
    private Stage stage;

    public void setReport(Report report) {
        this.currentReport = report;
        populateReportData();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void populateReportData() {
        if (currentReport == null)
            return;

        lblReportTitle.setText("📊 " + nullToEmpty(currentReport.getReportName()));
        lblReportType.setText(nullToEmpty(currentReport.getReportType()));
        lblAcademicYear.setText(nullToEmpty(currentReport.getAcademicYear()));

        if (currentReport.getGeneratedDate() != null) {
            lblGeneratedDate.setText(currentReport.getGeneratedDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        } else {
            lblGeneratedDate.setText("");
        }

        lblStatus.setText(nullToEmpty(currentReport.getStatus()));
        lblStatus.getStyleClass().removeIf(s -> s.startsWith("status-"));
        if (currentReport.getStatus() != null) {
            lblStatus.getStyleClass().add("status-" + currentReport.getStatus().toLowerCase());
        }

        txtReportContent.setText(nullToEmpty(currentReport.getSummaryData()));
    }

    @FXML
    private void handleExportPDF() {
        if (currentReport == null)
            return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report to PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(
                nullToEmpty(currentReport.getReportName()).replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                exportToPDF(file);
                showAlert("Success", "Report exported to PDF successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to export PDF: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportExcel() {
        if (currentReport == null)
            return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName(
                nullToEmpty(currentReport.getReportName()).replaceAll("[^a-zA-Z0-9]", "_") + ".xlsx");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                exportToExcel(file);
                showAlert("Success", "Report exported to Excel successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to export Excel: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrint() {
        showAlert("Info", "Print functionality coming soon!\n\nFor now, export to PDF and print from there.");
    }

    @FXML
    private void handleClose() {
        if (stage != null)
            stage.close();
    }

    private void exportToPDF(File file) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        try {
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(cleanText(currentReport.getReportName()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 11);
            contentStream.newLineAtOffset(50, 710);
            contentStream.showText(cleanText("Report Type: " + currentReport.getReportType()));
            contentStream.newLineAtOffset(0, -18);
            contentStream.showText(cleanText("Academic Year: " + currentReport.getAcademicYear()));
            contentStream.newLineAtOffset(0, -18);
            contentStream.showText(cleanText("Report Date: " + currentReport.getGeneratedDate()));
            contentStream.newLineAtOffset(0, -18);
            contentStream.showText(cleanText("Status: " + currentReport.getStatus()));
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(50, 640);
            String summary = currentReport.getSummaryData() == null ? "" : currentReport.getSummaryData();
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

    private void exportToExcel(File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Report");

        sheet.createRow(0).createCell(0).setCellValue(nullToEmpty(currentReport.getReportName()));
        Row metaRow1 = sheet.createRow(2);
        metaRow1.createCell(0).setCellValue("Report Type:");
        metaRow1.createCell(1).setCellValue(nullToEmpty(currentReport.getReportType()));
        metaRow1.createCell(2).setCellValue("Academic Year:");
        metaRow1.createCell(3).setCellValue(nullToEmpty(currentReport.getAcademicYear()));

        Row metaRow2 = sheet.createRow(3);
        metaRow2.createCell(0).setCellValue("Report Date:");
        metaRow2.createCell(1).setCellValue(
                currentReport.getGeneratedDate() == null ? "" : currentReport.getGeneratedDate().toString());
        metaRow2.createCell(2).setCellValue("Status:");
        metaRow2.createCell(3).setCellValue(nullToEmpty(currentReport.getStatus()));

        sheet.createRow(5).createCell(0).setCellValue("Report Content:");
        String summary = currentReport.getSummaryData() == null ? "" : currentReport.getSummaryData();
        int rowNum = 6;
        for (String line : summary.split("\n")) {
            sheet.createRow(rowNum++).createCell(0).setCellValue(line);
        }

        for (int i = 0; i < 4; i++)
            sheet.autoSizeColumn(i);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    private String cleanText(String text) {
        return text == null ? "" : text.replaceAll("[^\\x00-\\x7F]", "");
    }

    private String nullToEmpty(String text) {
        return text == null ? "" : text;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (stage != null)
            alert.initOwner(stage);
        alert.showAndWait();
    }
}