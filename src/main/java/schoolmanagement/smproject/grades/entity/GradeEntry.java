package schoolmanagement.smproject.grades.entity;

import javafx.beans.property.*;

public class GradeEntry {
    private final StringProperty studentName;
    private final DoubleProperty test1;
    private final DoubleProperty test2;
    private final DoubleProperty exam;
    private final DoubleProperty average;
    private int studentId;
    private String level;
    private String subject;

    public GradeEntry(String studentName) {
        this.studentName = new SimpleStringProperty(studentName);
        this.test1 = new SimpleDoubleProperty();
        this.test2 = new SimpleDoubleProperty();
        this.exam = new SimpleDoubleProperty();
        this.average = new SimpleDoubleProperty();
    }

    // Getters and Setters
    public String getStudentName() { return studentName.get(); }
    public StringProperty studentNameProperty() { return studentName; }

    public Double getTest1() { return test1.get(); }
    public void setTest1(Double test1) { this.test1.set(test1); }
    public DoubleProperty test1Property() { return test1; }

    public Double getTest2() { return test2.get(); }
    public void setTest2(Double test2) { this.test2.set(test2); }
    public DoubleProperty test2Property() { return test2; }

    public Double getExam() { return exam.get(); }
    public void setExam(Double exam) { this.exam.set(exam); }
    public DoubleProperty examProperty() { return exam; }

    public Double getAverage() { return average.get(); }
    public void setAverage(Double average) { this.average.set(average); }
    public DoubleProperty averageProperty() { return average; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
}