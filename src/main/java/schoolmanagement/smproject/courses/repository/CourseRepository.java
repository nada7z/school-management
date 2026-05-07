package schoolmanagement.smproject.courses.repository;

import schoolmanagement.smproject.courses.entity.Course;
import schoolmanagement.smproject.common.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CourseRepository {

    public Course save(Course course) {
        String sql = """
            INSERT INTO courses (course_code, name, description, level_id, teacher_id, 
                                 hours_per_week, status, max_capacity)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            int i = 1;
            stmt.setString(i++, course.getCourseCode());
            stmt.setString(i++, course.getName());
            stmt.setString(i++, course.getDescription());
            stmt.setInt(i++, course.getLevelId());
            
            if (course.getTeacherId() != null) {
                stmt.setInt(i++, course.getTeacherId());
            } else {
                stmt.setNull(i++, Types.INTEGER);
            }
            
            stmt.setInt(i++, course.getHoursPerWeek());
            stmt.setString(i++, course.getStatus());
            stmt.setInt(i++, course.getMaxCapacity());
            
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) course.setId(rs.getInt(1));
            }
            return course;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save course: " + e.getMessage(), e);
        }
    }

    public Optional<Course> findById(int id) {
        String sql = """
            SELECT c.*, l.name AS level_name, t.first_name || ' ' || t.last_name AS teacher_name
            FROM courses c
            LEFT JOIN levels l ON c.level_id = l.id
            LEFT JOIN teachers t ON c.teacher_id = t.id
            WHERE c.id = ?
            """;
        return executeQuerySingle(sql, stmt -> stmt.setInt(1, id));
    }

    public Optional<Course> findByCode(String code) {
        if (code == null || code.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT * FROM courses WHERE LOWER(course_code) = LOWER(?)";
        return executeQuerySingle(sql, stmt -> stmt.setString(1, code.trim()));
    }

    public List<Course> findAll() {
        String sql = """
            SELECT c.*, l.name AS level_name, 
                   CONCAT(t.first_name, ' ', t.last_name) AS teacher_name
            FROM courses c
            LEFT JOIN levels l ON c.level_id = l.id
            LEFT JOIN teachers t ON c.teacher_id = t.id
            ORDER BY c.course_code
            """;
        return executeQueryList(sql, stmt -> {});
    }

    public List<Course> findByLevelId(int levelId) {
        String sql = """
            SELECT c.*, l.name AS level_name,
                   CONCAT(t.first_name, ' ', t.last_name) AS teacher_name
            FROM courses c
            LEFT JOIN levels l ON c.level_id = l.id
            LEFT JOIN teachers t ON c.teacher_id = t.id
            WHERE c.level_id = ?
            ORDER BY c.name
            """;
        return executeQueryList(sql, stmt -> stmt.setInt(1, levelId));
    }

    public List<Course> findByTeacherId(int teacherId) {
        String sql = """
            SELECT c.*, l.name AS level_name,
                   CONCAT(t.first_name, ' ', t.last_name) AS teacher_name
            FROM courses c
            LEFT JOIN levels l ON c.level_id = l.id
            LEFT JOIN teachers t ON c.teacher_id = t.id
            WHERE c.teacher_id = ?
            ORDER BY c.name
            """;
        return executeQueryList(sql, stmt -> stmt.setInt(1, teacherId));
    }

    public List<Course> findByStatus(String status) {
        String sql = "SELECT * FROM courses WHERE status = ? ORDER BY name";
        return executeQueryList(sql, stmt -> stmt.setString(1, status));
    }

    public Course update(Course course) {
        String sql = """
            UPDATE courses SET course_code=?, name=?, description=?, level_id=?, teacher_id=?,
                               hours_per_week=?, status=?, max_capacity=?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int i = 1;
            stmt.setString(i++, course.getCourseCode());
            stmt.setString(i++, course.getName());
            stmt.setString(i++, course.getDescription());
            stmt.setInt(i++, course.getLevelId());
            
            if (course.getTeacherId() != null) {
                stmt.setInt(i++, course.getTeacherId());
            } else {
                stmt.setNull(i++, Types.INTEGER);
            }
            
            stmt.setInt(i++, course.getHoursPerWeek());
            stmt.setString(i++, course.getStatus());
            stmt.setInt(i++, course.getMaxCapacity());
            stmt.setInt(i, course.getId());
            
            stmt.executeUpdate();
            return course;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update course: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete course: " + e.getMessage(), e);
        }
    }

    // ✅ THIS IS THE METHOD YOUR DASHBOARD NEEDS
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM courses";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Count all failed: " + e.getMessage(), e);
        }
    }

    // ✅ THIS IS THE METHOD YOUR DASHBOARD NEEDS
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM courses WHERE status = 'Active'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Count active failed: " + e.getMessage(), e);
        }
    }

    // ======================== HELPERS ========================

    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setId(rs.getInt("id"));
        c.setCourseCode(rs.getString("course_code"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setLevelId(rs.getInt("level_id"));
        c.setLevelName(rs.getString("level_name"));
        c.setTeacherId(rs.getObject("teacher_id", Integer.class));
        c.setTeacherName(rs.getString("teacher_name"));
        c.setHoursPerWeek(rs.getInt("hours_per_week"));
        c.setStatus(rs.getString("status"));
        c.setMaxCapacity(rs.getInt("max_capacity"));
        return c;
    }

    private Optional<Course> executeQuerySingle(String sql, StatementSetter setter) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToCourse(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<Course> executeQueryList(String sql, StatementSetter setter) {
        List<Course> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCourse(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query list failed: " + e.getMessage(), e);
        }
        return list;
    }

    @FunctionalInterface
    private interface StatementSetter {
        void apply(PreparedStatement stmt) throws SQLException;
    }
}