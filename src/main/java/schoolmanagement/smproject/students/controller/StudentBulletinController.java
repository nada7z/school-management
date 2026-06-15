package schoolmanagement.smproject.students.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.grades.entity.Grade;
import schoolmanagement.smproject.grades.repository.GradeRepository;

// Excel Imports
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// PDF Imports
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentBulletinController {

    @FXML
    private Label userRoleLabel;
    @FXML
    private Button btnDashboard, btnStudents, btnTeachers, btnCourses, btnLevels, btnGrades, btnReports;

    @FXML
    private Label lblStudentName, lblStudentId, lblClassroom, lblAcademicYear;
    @FXML
    private Label lblTotalCoeff, lblTotalPoints, lblOverallAverage;
    @FXML
    private Label lblSubjectCount, lblStatusBadge, lblDecisionMessage;

    @FXML
    private TableView<Grade> gradesTable;
    @FXML
    private TableColumn<Grade, Integer> colIndex;
    @FXML
    private TableColumn<Grade, String> colSubject;
    @FXML
    private TableColumn<Grade, Double> colGrade;
    @FXML
    private TableColumn<Grade, Integer> colCoefficient;
    @FXML
    private TableColumn<Grade, Double> colProduct;

    private Student currentStudent;
    private GradeRepository gradeRepo;
    private final String academicYear = "2025-2026";

    @FXML
    public void initialize() {
        gradeRepo = new GradeRepository();
        setupTableColumns();
    }

    public void setStudent(Student student) {
        this.currentStudent = student;
        loadBulletin();
    }

    private void setupTableColumns() {
        gradesTable.setEditable(false); // Read-only

        colIndex.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                gradesTable.getItems().indexOf(data.getValue()) + 1));

        colSubject.setCellValueFactory(new PropertyValueFactory<>("subject"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("average"));
        colCoefficient.setCellValueFactory(new PropertyValueFactory<>("coefficient"));

        colProduct.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(
                data.getValue().getAverage() != null && data.getValue().getCoefficient() > 0
                        ? data.getValue().getAverage() * data.getValue().getCoefficient()
                        : 0.0));
    }

    private void loadBulletin() {
        if (currentStudent == null)
            return;

        lblStudentName.setText(currentStudent.getFullName());
        lblStudentId.setText("MAT-" + currentStudent.getId());
        lblClassroom.setText(currentStudent.getGradeLevel() +
                (currentStudent.getClassroom() != null ? "-" + currentStudent.getClassroom() : ""));
        lblAcademicYear.setText(academicYear);

        try {
            List<Grade> grades = gradeRepo.findByStudentId(currentStudent.getId());
            gradesTable.getItems().setAll(grades);
            recalculateTotals();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load bulletin: " + e.getMessage());
        }
    }

    private void recalculateTotals() {
        double totalPoints = 0;
        int totalCoeff = 0;
        int subjectCount = gradesTable.getItems().size();

        for (Grade g : gradesTable.getItems()) {
            Double grade = g.getAverage();
            int coef = g.getCoefficient() > 0 ? g.getCoefficient() : 1;
            if (grade != null) {
                totalPoints += grade * coef;
                totalCoeff += coef;
            }
        }

        double average = totalCoeff > 0 ? totalPoints / totalCoeff : 0;

        lblTotalCoeff.setText(String.valueOf(totalCoeff));
        lblTotalPoints.setText(String.format("%.2f", totalPoints));
        lblOverallAverage.setText(String.format("%.2f / 20", average));
        lblSubjectCount.setText(subjectCount + " matières");

        if (average >= 10) {
            lblStatusBadge.setText("ADMIS(E)");
            lblStatusBadge.getStyleClass().setAll("status-badge", "status-admitted");
            lblDecisionMessage.setText(String.format("Moyenne obtenue : %.2f / 20 — Félicitations ! 🎉", average));
            lblOverallAverage.setStyle("-fx-text-fill: #10b981;"); // Emerald Green
        } else {
            lblStatusBadge.setText("NON ADMIS(E)");
            lblStatusBadge.getStyleClass().setAll("status-badge", "status-failed");
            lblDecisionMessage.setText(String.format("Moyenne obtenue : %.2f / 20 — Efforts requis 📚", average));
            lblOverallAverage.setStyle("-fx-text-fill: #ef4444;"); // Red
        }
    }

    // ==========================================
    // ✅ EXPORT FUNCTIONS
    // ==========================================

    @FXML
    private void handleExportExcel() {
        if (gradesTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No grades to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Bulletin to Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("Bulletin_" + currentStudent.getFullName().replace(" ", "_") +
                "_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx");

        File file = fileChooser.showSaveDialog(gradesTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToExcel(file);
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
        if (gradesTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "No grades to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Bulletin to PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Bulletin_" + currentStudent.getFullName().replace(" ", "_") +
                "_" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf");

        File file = fileChooser.showSaveDialog(gradesTable.getScene().getWindow());
        if (file != null) {
            try {
                exportToPDF(file);
                showAlert(Alert.AlertType.INFORMATION, "Success ✅",
                        "PDF exported successfully to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export PDF:\n" + e.getMessage());
            }
        }
    }

    private void exportToExcel(File file) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Bulletin");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);

        Row headerRow = sheet.createRow(0);
        String[] headers = { "#", "Matière", "Note / 20", "Coef", "Produit" };
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Grade grade : gradesTable.getItems()) {
            Row row = sheet.createRow(rowNum++);
            double note = grade.getAverage() != null ? grade.getAverage() : 0.0;
            int coef = grade.getCoefficient() > 0 ? grade.getCoefficient() : 1;
            double produit = note * coef;

            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue(grade.getSubject());
            row.createCell(2).setCellValue(note);
            row.createCell(3).setCellValue(coef);
            row.createCell(4).setCellValue(produit);

            for (int i = 0; i < 5; i++)
                row.getCell(i).setCellStyle(cellStyle);
        }

        for (int i = 0; i < headers.length; i++)
            sheet.autoSizeColumn(i);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
        workbook.close();
    }

    private void exportToPDF(File file) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDFont fontBold = PDType1Font.HELVETICA_BOLD;
        PDFont fontRegular = PDType1Font.HELVETICA;

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float margin = 50;
            float yPosition = page.getMediaBox().getHeight() - margin;
            float tableWidth = page.getMediaBox().getWidth() - 2 * margin;

            contentStream.beginText();
            contentStream.setFont(fontBold, 18);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("BULLETIN SCOLAIRE");
            contentStream.endText();
            yPosition -= 30;

            contentStream.setFont(fontRegular, 11);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(cleanText(
                    "Nom: " + currentStudent.getFullName() + "      |       Matricule: " + currentStudent.getId()));
            contentStream.newLineAtOffset(0, -20);
            contentStream
                    .showText(cleanText("Classe: " + lblClassroom.getText() + "      |       Année: " + academicYear));
            contentStream.endText();
            yPosition -= 40;

            contentStream.setStrokingColor(0.2f, 0.4f, 0.6f);
            contentStream.setLineWidth(1.5f);
            contentStream.addRect(margin, yPosition - 25, tableWidth, 25);
            contentStream.stroke();

            contentStream.beginText();
            contentStream.setFont(fontBold, 10);
            contentStream.setNonStrokingColor(1, 1, 1);
            contentStream.newLineAtOffset(margin + 10, yPosition - 15);
            contentStream.showText("Matière");
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText("Note");
            contentStream.newLineAtOffset(60, 0);
            contentStream.showText("Coef");
            contentStream.newLineAtOffset(60, 0);
            contentStream.showText("Produit");
            contentStream.endText();
            contentStream.setNonStrokingColor(0, 0, 0);
            yPosition -= 30;

            contentStream.setFont(fontRegular, 9);
            for (Grade grade : gradesTable.getItems()) {
                double note = grade.getAverage() != null ? grade.getAverage() : 0.0;
                int coef = grade.getCoefficient() > 0 ? grade.getCoefficient() : 1;
                double produit = note * coef;

                contentStream.beginText();
                contentStream.newLineAtOffset(margin + 10, yPosition - 12);
                contentStream.showText(cleanText(grade.getSubject()));
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(String.format("%.2f", note));
                contentStream.newLineAtOffset(60, 0);
                contentStream.showText(String.valueOf(coef));
                contentStream.newLineAtOffset(60, 0);
                contentStream.showText(String.format("%.2f", produit));
                contentStream.endText();
                yPosition -= 20;
            }

            yPosition -= 30;
            contentStream.setFont(fontBold, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(cleanText("Moyenne Générale: " + lblOverallAverage.getText()));
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(cleanText("Décision: " + lblStatusBadge.getText()));
            contentStream.endText();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            document.save(file);
            document.close();
        }
    }

    // ==========================================
    // NAVIGATION & UTILS
    // ==========================================

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
    private void handleReports() {
        loadView("/reports.fxml");
    }

    @FXML
    private void handleLogout() {
        if (new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?").showAndWait().get() == ButtonType.YES) {
            loadView("/login.fxml");
        }
    }

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

    // ✅ Normalizes French accents so PDFBox doesn't crash when rendering Helvetica
    private String cleanText(String text) {
        if (text == null)
            return "";
        return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("[^\\x00-\\x7F]", "");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}