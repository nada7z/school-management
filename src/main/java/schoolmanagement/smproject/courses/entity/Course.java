package schoolmanagement.smproject.courses.entity;
import schoolmanagement.smproject.students.entity.Student;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an academic course/subject offering.
 * Maps to the 'courses' database table.
 */
public class Course {
    private int id;
    private String courseCode;          // e.g., "MATH-CE2", "FR-101"
    private String name;                // e.g., "Mathematics", "French Literature"
    private String description;
    private int levelId;                // Links to levels table (CE1-CE6)
    private String levelName;           // Populated via JOIN in repository
    private Integer teacherId;          // Primary instructor (nullable)
    private String teacherName;         // Populated via JOIN
    private int hoursPerWeek;           // Instructional time
    private String status;              // "Active", "Inactive", "Archived"
    private int maxCapacity;            // Enrollment limit
    private int currentEnrollment;      // Calculated via COUNT in repository
    
    // Optional: For future UI/Repo joins
    private List<Student> enrolledStudents = new ArrayList<>();

    // 🔹 Constructors
    public Course() {
        this.status = "Active";
    }

    public Course(String courseCode, String name, int levelId, int hoursPerWeek, int maxCapacity) {
        this();
        this.courseCode = courseCode;
        this.name = name;
        this.levelId = levelId;
        this.hoursPerWeek = hoursPerWeek;
        this.maxCapacity = maxCapacity;
    }

    // 🔹 Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getLevelId() { return levelId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public int getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(int hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public int getCurrentEnrollment() { return currentEnrollment; }
    public void setCurrentEnrollment(int currentEnrollment) { this.currentEnrollment = currentEnrollment; }

    public List<Student> getEnrolledStudents() { return enrolledStudents; }
    public void setEnrolledStudents(List<Student> enrolledStudents) { this.enrolledStudents = enrolledStudents; }

    // 🔹 UI / Business Helper Methods
    public String getDisplayLabel() {
        return courseCode + " - " + name;
    }

    public String getFullTitle() {
        String lvl = (levelName != null && !levelName.isEmpty()) ? levelName : "Level " + levelId;
        return getDisplayLabel() + " (" + lvl + ")";
    }

    public double getEnrollmentPercentage() {
        return maxCapacity > 0 ? (currentEnrollment * 100.0 / maxCapacity) : 0;
    }

    public boolean isFull() {
        return currentEnrollment >= maxCapacity;
    }

    public int getAvailableSeats() {
        return Math.max(0, maxCapacity - currentEnrollment);
    }

    public String getCapacityStatus() {
        double pct = getEnrollmentPercentage();
        if (pct >= 100) return "FULL";
        if (pct >= 85) return "ALMOST FULL";
        if (pct >= 50) return "MODERATE";
        return "AVAILABLE";
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", code='" + courseCode + '\'' +
                ", name='" + name + '\'' +
                ", level='" + getLevelName() + '\'' +
                ", teacher='" + teacherName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}