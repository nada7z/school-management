package schoolmanagement.smproject.classes.entity;

/**
 * Entity representing an academic level (e.g., CE1, CE2, CE3...).
 * Maps to the 'levels' database table.
 * Note: studentCount is dynamically calculated via repository JOIN query.
 */
public class Level {
    private int id;
    private String name;            // e.g., "CE1", "CE2", "CE3"
    private String description;     // e.g., "Primary Cycle 1"
    private int sortOrder;          // Controls UI ordering (1=CE1, 2=CE2, etc.)
    private long studentCount;      // Populated by LevelRepository.findAllWithCounts()

    // 🔹 Constructors
    public Level() {}

    public Level(int id, String name, String description, int sortOrder) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
        this.studentCount = 0;
    }

    // 🔹 Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public long getStudentCount() { return studentCount; }
    public void setStudentCount(long studentCount) { this.studentCount = studentCount; }

    // 🔹 UI / Business Helper Methods
    public String getDisplayLabel() {
        return name + (description != null && !description.isEmpty() ? " • " + description : "");
    }

    public String getEnrollmentSummary() {
        return studentCount + " student" + (studentCount == 1 ? "" : "s") + " enrolled";
    }

    /** Returns a consistent accent color for UI cards (matches LevelsController logic) */
    public String getAccentColor() {
        return switch (sortOrder) {
            case 1 -> "#3b82f6"; // CE1 Blue
            case 2 -> "#10b981"; // CE2 Green
            case 3 -> "#f59e0b"; // CE3 Amber
            case 4 -> "#8b5cf6"; // CE4 Purple
            case 5 -> "#ec4899"; // CE5 Pink
            case 6 -> "#06b6d4"; // CE6 Cyan
            default -> "#64748b"; // Fallback Gray
        };
    }

    public boolean hasStudents() {
        return studentCount > 0;
    }

    @Override
    public String toString() {
        return "Level{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sortOrder=" + sortOrder +
                ", studentCount=" + studentCount +
                '}';
    }
}