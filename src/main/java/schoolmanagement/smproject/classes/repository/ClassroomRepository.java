package schoolmanagement.smproject.classes.repository;

import schoolmanagement.smproject.classes.entity.Classroom;
import schoolmanagement.smproject.common.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClassroomRepository {

    /**
     * Fetches all classrooms for a specific academic level (e.g., CE1, CE2)
     * Includes level name and live student count via subquery.
     */
    public List<Classroom> findByLevelId(int levelId) {
        String sql = """
            SELECT c.id, c.level_id, l.name AS level_name, c.section,
                   c.teacher_id, c.max_capacity,
                   (SELECT COUNT(*) FROM students s WHERE s.classroom_id = c.id) AS current_enrollment
            FROM classrooms c
            JOIN levels l ON c.level_id = l.id
            WHERE c.level_id = ?
            ORDER BY c.section ASC
            """;
        return executeQueryList(sql, stmt -> stmt.setInt(1, levelId));
    }

    /**
     * Fetches a single classroom by ID
     */
    public Optional<Classroom> findById(int id) {
        String sql = """
            SELECT c.id, c.level_id, l.name AS level_name, c.section,
                   c.teacher_id, c.max_capacity,
                   (SELECT COUNT(*) FROM students s WHERE s.classroom_id = c.id) AS current_enrollment
            FROM classrooms c
            JOIN levels l ON c.level_id = l.id
            WHERE c.id = ?
            """;
        return executeQuerySingle(sql, stmt -> stmt.setInt(1, id));
    }

    /**
     * Fetches all classrooms across all levels, ordered by level then section
     */
    public List<Classroom> findAll() {
        String sql = """
            SELECT c.id, c.level_id, l.name AS level_name, c.section,
                   c.teacher_id, c.max_capacity,
                   (SELECT COUNT(*) FROM students s WHERE s.classroom_id = c.id) AS current_enrollment
            FROM classrooms c
            JOIN levels l ON c.level_id = l.id
            ORDER BY l.sort_order, c.section ASC
            """;
        return executeQueryList(sql, stmt -> {});
    }

    /**
     * Inserts a new classroom and returns it with the generated ID
     */
    public Classroom save(Classroom classroom) {
        String sql = "INSERT INTO classrooms (level_id, section, teacher_id, max_capacity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, classroom.getLevelId());
            stmt.setString(2, classroom.getSection());
            
            if (classroom.getTeacherId() != null) {
                stmt.setInt(3, classroom.getTeacherId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setInt(4, classroom.getMaxCapacity());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) classroom.setId(rs.getInt(1));
            }
            return classroom;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save classroom: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing classroom
     */
    public Classroom update(Classroom classroom) {
        String sql = "UPDATE classrooms SET level_id=?, section=?, teacher_id=?, max_capacity=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, classroom.getLevelId());
            stmt.setString(2, classroom.getSection());
            
            if (classroom.getTeacherId() != null) {
                stmt.setInt(3, classroom.getTeacherId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            
            stmt.setInt(4, classroom.getMaxCapacity());
            stmt.setInt(5, classroom.getId());
            stmt.executeUpdate();
            return classroom;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update classroom: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a classroom by ID
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM classrooms WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete classroom: " + e.getMessage(), e);
        }
    }

    // ======================== HELPER METHODS ========================

    private Classroom mapResultSetToClassroom(ResultSet rs) throws SQLException {
        Classroom c = new Classroom();
        c.setId(rs.getInt("id"));
        c.setLevelId(rs.getInt("level_id"));
        c.setLevelName(rs.getString("level_name"));
        c.setSection(rs.getString("section"));
        // Safely handles nullable teacher_id
        c.setTeacherId(rs.getObject("teacher_id", Integer.class));
        c.setMaxCapacity(rs.getInt("max_capacity"));
        c.setCurrentEnrollment(rs.getInt("current_enrollment"));
        return c;
    }

    private Optional<Classroom> executeQuerySingle(String sql, StatementSetter setter) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToClassroom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<Classroom> executeQueryList(String sql, StatementSetter setter) {
        List<Classroom> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClassroom(rs));
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