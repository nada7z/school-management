package schoolmanagement.smproject.courses.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import schoolmanagement.smproject.courses.entity.Course;
import schoolmanagement.smproject.classes.entity.Level;
import schoolmanagement.smproject.teachers.entity.Teacher;
import schoolmanagement.smproject.courses.repository.CourseRepository;
import schoolmanagement.smproject.classes.repository.LevelRepository;
import schoolmanagement.smproject.teachers.repository.TeacherRepository;

import java.io.IOException;
import java.util.List;

public class CreateCourseController {

    @FXML
    private Label userRoleLabel;
    @FXML
    private Button btnDashboard, btnCourses, btnStudents, btnTeachers, btnLevels;

    @FXML
    private TextField txtCourseCode, txtCourseName;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<Level> cbLevel;
    @FXML
    private ComboBox<Teacher> cbTeacher;
    @FXML
    private Spinner<Integer> spnHours, spnCapacity;
    @FXML
    private ComboBox<String> cbStatus;

    private Course editingCourse = null;

    @FXML
    public void initialize() {
        setupSpinners();
        loadComboBoxes();
        cbStatus.getSelectionModel().select("Active");
    }

    private void setupSpinners() {
        SpinnerValueFactory<Integer> hoursFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2);
        spnHours.setValueFactory(hoursFactory);

        SpinnerValueFactory<Integer> capacityFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 100, 30);
        spnCapacity.setValueFactory(capacityFactory);
    }

    private void loadComboBoxes() {
        try {
            LevelRepository levelRepo = new LevelRepository();
            TeacherRepository teacherRepo = new TeacherRepository();

            List<Level> levels = levelRepo.findAll();
            cbLevel.getItems().setAll(levels);

            List<Teacher> teachers = teacherRepo.findAll();
            cbTeacher.getItems().setAll(teachers);
            cbTeacher.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Teacher teacher, boolean empty) {
                    super.updateItem(teacher, empty);
                    setText(empty || teacher == null ? null
                            : teacher.getFullName() + " • " + teacher.getSubjectSpecialization());
                }
            });
            cbTeacher.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Teacher teacher, boolean empty) {
                    super.updateItem(teacher, empty);
                    setText(empty || teacher == null ? "Select Teacher" : teacher.getFullName());
                }
            });

            cbLevel.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Level level, boolean empty) {
                    super.updateItem(level, empty);
                    setText(empty || level == null ? null : level.getName());
                }
            });
            cbLevel.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Level level, boolean empty) {
                    super.updateItem(level, empty);
                    setText(empty || level == null ? "Select Level" : level.getName());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSaveCourse() {
        if (!validateForm())
            return;

        try {
            Course course = editingCourse != null ? editingCourse : new Course();

            course.setCourseCode(txtCourseCode.getText().trim().toUpperCase());
            course.setName(txtCourseName.getText().trim());
            course.setDescription(txtDescription.getText().trim());
            course.setLevelId(cbLevel.getValue().getId());

            Teacher selectedTeacher = cbTeacher.getValue();
            course.setTeacherId(selectedTeacher != null ? selectedTeacher.getId() : null);

            course.setHoursPerWeek(spnHours.getValue());
            course.setMaxCapacity(spnCapacity.getValue());
            course.setStatus(cbStatus.getValue());

            CourseRepository repo = new CourseRepository();

            if (editingCourse == null) {
                repo.save(course);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course created successfully!");
            } else {
                repo.update(course);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated successfully!");
            }

            handleCourses();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save course: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtCourseCode.getText().trim().isEmpty()) {
            errors.append("• Course Code is required\n");
        }
        if (txtCourseName.getText().trim().isEmpty()) {
            errors.append("• Course Name is required\n");
        }
        if (cbLevel.getValue() == null) {
            errors.append("• Academic Level is required\n");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", errors.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void handleReset() {
        txtCourseCode.clear();
        txtCourseName.clear();
        txtDescription.clear();
        cbLevel.getSelectionModel().clearSelection();
        cbTeacher.getSelectionModel().clearSelection();
        spnHours.getValueFactory().setValue(2);
        spnCapacity.getValueFactory().setValue(30);
        cbStatus.getSelectionModel().select("Active");
        txtCourseCode.requestFocus();
    }

    @FXML
    private void handleCancel() {
        handleCourses();
    }

    @FXML
    private void handleDashboard() {
        loadView("/dashboard.fxml");
    }

    @FXML
    private void handleCourses() {
        loadView("/courses.fxml");
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
    private void handleLevels() {
        loadView("/levels.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirm logout?");
        if (alert.showAndWait().get() == ButtonType.YES) {
            loadView("/login.fxml");
        }
    }

    public void setCourseForEdit(Course course) {
        this.editingCourse = course;

        txtCourseCode.setText(course.getCourseCode());
        txtCourseName.setText(course.getName());
        txtDescription.setText(course.getDescription());

        cbLevel.getItems().stream()
                .filter(level -> level.getId() == course.getLevelId())
                .findFirst()
                .ifPresent(level -> cbLevel.getSelectionModel().select(level));

        cbTeacher.getItems().stream()
                .filter(teacher -> course.getTeacherId() != null && teacher.getId() == course.getTeacherId())
                .findFirst()
                .ifPresent(teacher -> cbTeacher.getSelectionModel().select(teacher));

        spnHours.getValueFactory().setValue(course.getHoursPerWeek());
        spnCapacity.getValueFactory().setValue(course.getMaxCapacity());
        cbStatus.getSelectionModel().select(course.getStatus());
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

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}