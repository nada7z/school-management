package schoolmanagement.smproject.classes.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import schoolmanagement.smproject.classes.entity.Level;
import schoolmanagement.smproject.classes.repository.LevelRepository;

import java.io.IOException;
import java.util.List;

public class LevelsController {
    @FXML private VBox levelsContainer;

    @FXML
    public void initialize() {
        try {
            LevelRepository repo = new LevelRepository();
            List<Level> levels = repo.findAllWithCounts();
            if (levels.isEmpty()) levels = createDefaultLevels(); // Fallback if DB empty
            
            for (Level level : levels) {
                levelsContainer.getChildren().add(createLevelCard(level));
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load levels: " + e.getMessage());
        }
    }

    private List<Level> createDefaultLevels() {
        return List.of(
            createLevel(1, "CE1", "Primary Cycle 1"),
            createLevel(2, "CE2", "Primary Cycle 2"),
            createLevel(3, "CE3", "Primary Cycle 3"),
            createLevel(4, "CE4", "Primary Cycle 4"),
            createLevel(5, "CE5", "Primary Cycle 5"),
            createLevel(6, "CE6", "Primary Cycle 6")
        );
    }

    private Level createLevel(int id, String name, String desc) {
        Level l = new Level();
        l.setId(id); l.setName(name); l.setDescription(desc); 
        l.setSortOrder(id); l.setStudentCount(0);
        return l;
    }

    private VBox createLevelCard(Level level) {
        VBox card = new VBox(12);
        card.getStyleClass().add("level-card");
        card.setStyle("-fx-cursor: hand;");
        card.setOnMouseClicked(e -> navigateToClasses(level));

        String accent = getLevelColor(level.getSortOrder());

        HBox header = new HBox(12);
        Label icon = new Label("📖");
        icon.setStyle("-fx-font-size: 28; -fx-background-color: " + accent + "22; -fx-background-radius: 12; -fx-padding: 8 12;");
        Label name = new Label(level.getName());
        name.getStyleClass().add("level-name");
        header.getChildren().addAll(icon, name);

        Label desc = new Label(level.getDescription() != null ? level.getDescription() : "");
        desc.getStyleClass().add("level-desc");

        Label count = new Label(level.getStudentCount() + " students");
        count.getStyleClass().add("student-count");
        count.setStyle(count.getStyle() + "-fx-text-fill: " + accent + ";");

        card.getChildren().addAll(header, desc, count);
        return card;
    }

    private String getLevelColor(int order) {
        return switch (order) {
            case 1 -> "#3b82f6"; case 2 -> "#10b981"; case 3 -> "#f59e0b";
            case 4 -> "#8b5cf6"; case 5 -> "#ec4899"; case 6 -> "#06b6d4";
            default -> "#64748b";
        };
    }

    private void navigateToClasses(Level level) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/classes.fxml"));
            Parent root = loader.load();
            
            // 👇 CRITICAL: Pass data AFTER load()
            ClassesController controller = loader.getController();
            controller.setCurrentLevel(level);

            Stage stage = (Stage) levelsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("School MS ➜ " + level.getName() + " Classes");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Failed to load classes view.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg);
        alert.showAndWait();
    }
}