package schoolmanagement.smproject.classes.entity;

/**
 * Entity representing a classroom/section within an academic level.
 * Maps to the 'classrooms' database table + joined/calculated fields.
 */
public class Classroom {
    private int id;
    private int levelId;
    private String levelName;        // Populated via JOIN in repository
    private String section;          // e.g., "A", "B", "1", "2"
    private Integer teacherId;       // Nullable (FK to teachers table)
    private int maxCapacity;
    private int currentEnrollment;   // Populated via COUNT subquery in repository

    // 🔹 Constructors
    public Classroom() {}

    public Classroom(int id, int levelId, String section, int maxCapacity) {
        this.id = id;
        this.levelId = levelId;
        this.section = section;
        this.maxCapacity = maxCapacity;
    }

    // 🔹 Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLevelId() { return levelId; }
    public void setLevelId(int levelId) { this.levelId = levelId; }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public int getCurrentEnrollment() { return currentEnrollment; }
    public void setCurrentEnrollment(int currentEnrollment) { this.currentEnrollment = currentEnrollment; }

    // 🔹 Computed / UI Helper Methods
    public String getFullName() {
        String base = (levelName != null && !levelName.isEmpty()) ? levelName : "CE" + levelId;
        return base + " - " + section;
    }

    public double getCapacityPercentage() {
        return maxCapacity > 0 ? (currentEnrollment * 100.0 / maxCapacity) : 0;
    }

    public boolean isFull() {
        return currentEnrollment >= maxCapacity;
    }

    public int getAvailableSeats() {
        return Math.max(0, maxCapacity - currentEnrollment);
    }

    public String getCapacityStatus() {
        double pct = getCapacityPercentage();
        if (pct >= 100) return "FULL";
        if (pct >= 85) return "ALMOST FULL";
        if (pct >= 50) return "MODERATE";
        return "AVAILABLE";
    }

    @Override
    public String toString() {
        return "Classroom{" +
                "id=" + id +
                ", name='" + getFullName() + '\'' +
                ", enrollment=" + currentEnrollment + "/" + maxCapacity +
                ", teacherId=" + teacherId +
                '}';
    }
}