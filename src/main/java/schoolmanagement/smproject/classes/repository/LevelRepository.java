package schoolmanagement.smproject.classes.repository;

import schoolmanagement.smproject.classes.entity.Level;
import schoolmanagement.smproject.common.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LevelRepository {

    // ✅ THIS IS THE MISSING METHOD
    public List<Level> findAllWithCounts() {
        String sql = """
            SELECT l.id, l.name, l.description, l.sort_order, 
                   COUNT(s.id) AS student_count
            FROM levels l
            LEFT JOIN classrooms c ON l.id = c.level_id
            LEFT JOIN students s ON c.id = s.classroom_id
            GROUP BY l.id
            ORDER BY l.sort_order ASC
            """;
        return executeQueryList(sql, stmt -> {}, true);
    }

    public List<Level> findAll() {
        String sql = "SELECT id, name, description, sort_order FROM levels ORDER BY sort_order ASC";
        return executeQueryList(sql, stmt -> {}, false);
    }

    public Optional<Level> findById(int id) {
        String sql = "SELECT id, name, description, sort_order FROM levels WHERE id = ?";
        return executeQuerySingle(sql, stmt -> stmt.setInt(1, id));
    }

    public Optional<Level> findByName(String name) {
        if (name == null || name.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT id, name, description, sort_order FROM levels WHERE LOWER(name) = LOWER(?)";
        return executeQuerySingle(sql, stmt -> stmt.setString(1, name.trim()));
    }

    public Level save(Level level) {
        String sql = "INSERT INTO levels (name, description, sort_order) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, level.getName());
            stmt.setString(2, level.getDescription() != null ? level.getDescription() : "");
            stmt.setInt(3, level.getSortOrder());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) level.setId(rs.getInt(1));
            }
            return level;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save level: " + e.getMessage(), e);
        }
    }

    public Level update(Level level) {
        String sql = "UPDATE levels SET name = ?, description = ?, sort_order = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, level.getName());
            stmt.setString(2, level.getDescription() != null ? level.getDescription() : "");
            stmt.setInt(3, level.getSortOrder());
            stmt.setInt(4, level.getId());
            stmt.executeUpdate();
            return level;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update level: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM levels WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete level: " + e.getMessage(), e);
        }
    }

    // ======================== HELPERS ========================

    private Level mapResultSetToLevel(ResultSet rs, boolean includeCount) throws SQLException {
        Level level = new Level();
        level.setId(rs.getInt("id"));
        level.setName(rs.getString("name"));
        level.setDescription(rs.getString("description"));
        level.setSortOrder(rs.getInt("sort_order"));
        if (includeCount) {
            level.setStudentCount(rs.getLong("student_count"));
        }
        return level;
    }

    private Optional<Level> executeQuerySingle(String sql, StatementSetter setter) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToLevel(rs, false));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<Level> executeQueryList(String sql, StatementSetter setter) {
        return executeQueryList(sql, setter, false);
    }

    private List<Level> executeQueryList(String sql, StatementSetter setter, boolean includeCount) {
        List<Level> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLevel(rs, includeCount));
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