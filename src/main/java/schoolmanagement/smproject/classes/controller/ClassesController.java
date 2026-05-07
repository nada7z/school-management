package schoolmanagement.smproject.classes.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import schoolmanagement.smproject.classes.entity.Classroom;
import schoolmanagement.smproject.classes.entity.Level;
import schoolmanagement.smproject.classes.repository.ClassroomRepository;

import java.io.IOException;

public class ClassesController {
    @FXML private VBox classesContainer;
    @FXML private Label levelTitle;
    @FXML private Button btnBack;

    private Level currentLevel;

    // 👇 Called by LevelsController immediately after FXML loads
    public void setCurrentLevel(Level level) {
        this.currentLevel = level;
        loadClassrooms(); // Load data only after level is set
    }

    private void loadClassrooms() {
        if (currentLevel == null) return;
        levelTitle.setText("📚 " + currentLevel.getName() + " Classrooms");
        classesContainer.getChildren().clear();

        try {
            ClassroomRepository repo = new ClassroomRepository();
            for (Classroom c : repo.findByLevelId(currentLevel.getId())) {
                classesContainer.getChildren().add(createClassCard(c));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load classrooms: " + e.getMessage());
        }
    }

    private VBox createClassCard(Classroom c) {
        VBox card = new VBox(14);
        card.getStyleClass().add("class-card");
        card.setStyle("-fx-cursor: hand; -fx-padding: 20;");
        card.setOnMouseClicked(e -> navigateToStudents(c));

        Label title = new Label(c.getFullName());
        title.getStyleClass().add("class-name");

        ProgressBar bar = new ProgressBar(c.getCapacityPercentage() / 100.0);
        bar.setPrefWidth(250);
        bar.setStyle("-fx-accent: #3b82f6;");

        Label meta = new Label(c.getCurrentEnrollment() + " / " + c.getMaxCapacity() + " students • " + c.getCapacityStatus());
        meta.getStyleClass().add("class-meta");

        HBox bottom = new HBox(16, bar, meta);
        bottom.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        card.getChildren().addAll(title, bottom);
        return card;
    }

    private void navigateToStudents(Classroom classroom) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/classStudents.fxml"));
            Parent root = loader.load();
            
            // 👇 Pass classroom to student table controller
            schoolmanagement.smproject.students.controller.ClassStudentsController ctrl = loader.getController();
            ctrl.setCurrentClass(classroom);

            Stage stage = (Stage) classesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("School MS ➜ " + classroom.getFullName());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load students view.");
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/levels.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("School MS ➜ Academic Levels");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg);
        alert.showAndWait();
    }
}