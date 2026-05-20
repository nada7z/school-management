package schoolmanagement.smproject.grades.entity;

/**
 * Entity representing a student's grades for a specific subject.
 * Maps to the 'grades' database table.
 */
public class Grade {
    private int id;
    private int studentId;
    private String studentName; // Joined from students table
    private int levelId;
    private String subject;
    private Double test1;
    private Double test2;
    private Double exam;
    private Double average; // Auto-calculated by DB
    private String academicYear;
    // Add this to schoolmanagement.smproject.grades.entity.Grade
    private int coefficient = 1; // Default coefficient

    public int getCoefficient() { return coefficient; }
    public void setCoefficient(int coefficient) { this.coefficient = coefficient; }
    // Constructors
    public Grade() {}

    public Grade(int studentId, String studentName, String subject) {
        this.studentId = studentId;  
        this.studentName = studentName;
        this.subject = subject;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public int getLevelId() { return levelId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Double getTest1() { return test1; }
    public void setTest1(Double test1) { this.test1 = test1; }

    public Double getTest2() { return test2; }
    public void setTest2(Double test2) { this.test2 = test2; }

    public Double getExam() { return exam; }
    public void setExam(Double exam) { this.exam = exam; }

    public Double getAverage() { return average; }
    public void setAverage(Double average) { this.average = average; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    @Override
    public String toString() {
        return "Grade{" +
                "student='" + studentName + '\'' +
                ", subject='" + subject + '\'' +
                ", avg=" + average +
                '}';
    }
}