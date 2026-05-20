package schoolmanagement.smproject.grades.repository;

import schoolmanagement.smproject.grades.entity.Grade;
import schoolmanagement.smproject.common.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GradeRepository {

    /**
     * Save or update a grade record
     */
    public Grade save(Grade grade) {
        String sql = """
            INSERT INTO grades (student_id, level_id, subject, test1, test2, exam, academic_year)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE 
                test1 = VALUES(test1),
                test2 = VALUES(test2),
                exam = VALUES(exam),
                updated_at = CURRENT_TIMESTAMP
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, grade.getStudentId());
            stmt.setInt(2, grade.getLevelId());
            stmt.setString(3, grade.getSubject());
            stmt.setObject(4, grade.getTest1());
            stmt.setObject(5, grade.getTest2());
            stmt.setObject(6, grade.getExam());
            stmt.setString(7, grade.getAcademicYear() != null ? grade.getAcademicYear() : "2025-2026");
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) grade.setId(rs.getInt(1));
            }
            return grade;
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save grade: " + e.getMessage(), e);
        }
    }

    /**
     * Find all grades for a specific level and subject
     */
    public List<Grade> findByLevelAndSubject(int levelId, String subject) {
        String sql = """
            SELECT g.id, g.student_id, s.first_name, s.last_name, 
                   g.level_id, g.subject, g.test1, g.test2, g.exam, g.average, g.academic_year
            FROM grades g
            JOIN students s ON g.student_id = s.id
            WHERE g.level_id = ? AND g.subject = ?
            ORDER BY s.last_name, s.first_name
            """;
        
        return executeQueryList(sql, stmt -> {
            stmt.setInt(1, levelId);
            stmt.setString(2, subject);
        });
    }

    /**
     * Find grade by student and subject
     */
    public Optional<Grade> findByStudentAndSubject(int studentId, String subject, String academicYear) {
        String sql = """
            SELECT g.id, g.student_id, s.first_name, s.last_name,
                   g.level_id, g.subject, g.test1, g.test2, g.exam, g.average, g.academic_year
            FROM grades g
            JOIN students s ON g.student_id = s.id
            WHERE g.student_id = ? AND g.subject = ? AND g.academic_year = ?
            """;
        
        return executeQuerySingle(sql, stmt -> {
            stmt.setInt(1, studentId);
            stmt.setString(2, subject);
            stmt.setString(3, academicYear);
        });
    }

    /**
     * Get all grades for a student
     */
    public List<Grade> findByStudentId(int studentId) {
        String sql = """
            SELECT g.id, g.student_id, s.first_name, s.last_name,
                   g.level_id, g.subject, g.test1, g.test2, g.exam, g.average, g.academic_year
            FROM grades g
            JOIN students s ON g.student_id = s.id
            WHERE g.student_id = ?
            ORDER BY g.subject
            """;
        
        return executeQueryList(sql, stmt -> stmt.setInt(1, studentId));
    }

    // ======================== HELPERS ========================

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setId(rs.getInt("id"));
        grade.setStudentId(rs.getInt("student_id"));
        grade.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
        grade.setLevelId(rs.getInt("level_id"));
        grade.setSubject(rs.getString("subject"));
        grade.setTest1(rs.getObject("test1", Double.class));
        grade.setTest2(rs.getObject("test2", Double.class));
        grade.setExam(rs.getObject("exam", Double.class));
        grade.setAverage(rs.getObject("average", Double.class));
        grade.setAcademicYear(rs.getString("academic_year"));
        return grade;
    }

    private Optional<Grade> executeQuerySingle(String sql, StatementSetter setter) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToGrade(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<Grade> executeQueryList(String sql, StatementSetter setter) {
        List<Grade> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToGrade(rs));
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