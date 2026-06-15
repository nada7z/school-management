package schoolmanagement.smproject.reports.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import schoolmanagement.smproject.common.DatabaseConnection;
import schoolmanagement.smproject.reports.entity.Report;
import schoolmanagement.smproject.reports.repository.ReportRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class GenerateReportController {
    @FXML
    private TextField txtReportName;
    @FXML
    private ComboBox<String> cbReportType;
    @FXML
    private DatePicker dpReportDate;
    @FXML
    private TextArea txtReportContent;
    @FXML
    private Button btnGenerate;

    private Report editingReport = null;

    @FXML
    public void initialize() {
        cbReportType.getItems().addAll("Enrollment Report", "Attendance Summary", "Academic Performance",
                "Financial Report", "General Report");
        cbReportType.getSelectionModel().selectFirst();
        dpReportDate.setValue(LocalDate.now());
    }

    @FXML
    private void handleGenerate() {
        if (txtReportName.getText() == null || txtReportName.getText().trim().isEmpty()) {
            showAlert("⚠️ Please enter a report title.");
            return;
        }
        if (txtReportContent.getText() == null || txtReportContent.getText().trim().isEmpty()) {
            showAlert("⚠️ Please enter report content.");
            return;
        }

        String title = txtReportName.getText().trim();
        String type = cbReportType.getValue();
        LocalDate date = dpReportDate.getValue();
        String content = txtReportContent.getText().trim();

        try {
            if (editingReport == null) {
                saveReportToDatabase(title, type, date, content);
                exportToPDF(title, content);
                showAlert("✅ Report generated and saved successfully!");
            } else {
                editingReport.setReportName(title);
                editingReport.setReportType(type);
                editingReport.setGeneratedDate(date);
                editingReport.setSummaryData(content);
                if (editingReport.getStatus() == null)
                    editingReport.setStatus("Generated");

                new ReportRepository().update(editingReport);
                showAlert("✅ Report updated successfully!");
            }
            closeWindow();
        } catch (Exception e) {
            showAlert("❌ Error: " + e.getMessage());
        }
    }

    private void saveReportToDatabase(String title, String type, LocalDate date, String content) throws SQLException {
        String sql = "INSERT INTO reports (report_title, report_type, report_date, content, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, type);
            stmt.setObject(3, date);
            stmt.setString(4, content);
            stmt.executeUpdate();
        }
    }

    private void exportToPDF(String title, String content) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName(title.replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");

        File file = fileChooser.showSaveDialog(btnGenerate.getScene().getWindow());
        if (file == null)
            return;

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream stream = new PDPageContentStream(document, page);

        try {
            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            stream.newLineAtOffset(50, 750);
            stream.showText(cleanText(title));
            stream.endText();

            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA, 12);
            stream.newLineAtOffset(50, 700);
            for (String line : content.split("\n")) {
                stream.showText(cleanText(line));
                stream.newLineAtOffset(0, -18);
            }
            stream.endText();
        } finally {
            stream.close();
        }

        document.save(file);
        document.close();
    }

    public void setReportForEdit(Report report) {
        this.editingReport = report;
        txtReportName.setText(report.getReportName());
        cbReportType.getSelectionModel().select(report.getReportType());
        dpReportDate.setValue(report.getGeneratedDate());
        txtReportContent.setText(report.getSummaryData());
        btnGenerate.setText("Update Report");
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) btnGenerate.getScene().getWindow()).close();
    }

    private String cleanText(String text) {
        return text == null ? "" : text.replaceAll("[^\\x00-\\x7F]", "");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Generator");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}