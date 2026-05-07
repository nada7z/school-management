package schoolmanagement.smproject.teachers.repository;

import schoolmanagement.smproject.teachers.entity.Teacher;
import schoolmanagement.smproject.common.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeacherRepository {

    public Teacher save(Teacher teacher) {
        String sql = """
            INSERT INTO teachers (employee_id, first_name, last_name, email, phone, 
                                  date_of_birth, gender, address, subject_specialization, 
                                  qualification, hire_date, status, emergency_contact_name, emergency_contact_phone)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            int i = 1;
            stmt.setString(i++, teacher.getEmployeeId());
            stmt.setString(i++, teacher.getFirstName());
            stmt.setString(i++, teacher.getLastName());
            stmt.setString(i++, teacher.getEmail());
            stmt.setString(i++, teacher.getPhone());
            stmt.setObject(i++, teacher.getDateOfBirth());
            stmt.setString(i++, teacher.getGender());
            stmt.setString(i++, teacher.getAddress());
            stmt.setString(i++, teacher.getSubjectSpecialization());
            stmt.setString(i++, teacher.getQualification());
            stmt.setObject(i++, teacher.getHireDate());
            stmt.setString(i++, teacher.getStatus());
            stmt.setString(i++, teacher.getEmergencyContactName());
            stmt.setString(i++, teacher.getEmergencyContactPhone());
            
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) teacher.setId(rs.getInt(1));
            }
            return teacher;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save teacher: " + e.getMessage(), e);
        }
    }

    public Optional<Teacher> findById(int id) {
        String sql = "SELECT * FROM teachers WHERE id = ?";
        return executeQuerySingle(sql, stmt -> stmt.setInt(1, id));
    }

    public Optional<Teacher> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT * FROM teachers WHERE LOWER(email) = LOWER(?)";
        return executeQuerySingle(sql, stmt -> stmt.setString(1, email.trim()));
    }

    public List<Teacher> findAll() {
        String sql = "SELECT * FROM teachers ORDER BY last_name, first_name";
        return executeQueryList(sql, stmt -> {});
    }

    public List<Teacher> findBySubject(String subject) {
        if (subject == null || subject.trim().isEmpty()) return List.of();
        String sql = "SELECT * FROM teachers WHERE LOWER(subject_specialization) LIKE LOWER(?) ORDER BY last_name";
        return executeQueryList(sql, stmt -> stmt.setString(1, "%" + subject.trim() + "%"));
    }

    public List<Teacher> findByStatus(String status) {
        if (status == null || status.trim().isEmpty()) return List.of();
        String sql = "SELECT * FROM teachers WHERE status = ? ORDER BY last_name";
        return executeQueryList(sql, stmt -> stmt.setString(1, status));
    }

    public Teacher update(Teacher teacher) {
        String sql = """
            UPDATE teachers SET employee_id=?, first_name=?, last_name=?, email=?, phone=?,
                                date_of_birth=?, gender=?, address=?, subject_specialization=?,
                                qualification=?, hire_date=?, status=?, emergency_contact_name=?, emergency_contact_phone=?
            WHERE id = ?
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int i = 1;
            stmt.setString(i++, teacher.getEmployeeId());
            stmt.setString(i++, teacher.getFirstName());
            stmt.setString(i++, teacher.getLastName());
            stmt.setString(i++, teacher.getEmail());
            stmt.setString(i++, teacher.getPhone());
            stmt.setObject(i++, teacher.getDateOfBirth());
            stmt.setString(i++, teacher.getGender());
            stmt.setString(i++, teacher.getAddress());
            stmt.setString(i++, teacher.getSubjectSpecialization());
            stmt.setString(i++, teacher.getQualification());
            stmt.setObject(i++, teacher.getHireDate());
            stmt.setString(i++, teacher.getStatus());
            stmt.setString(i++, teacher.getEmergencyContactName());
            stmt.setString(i++, teacher.getEmergencyContactPhone());
            stmt.setInt(i, teacher.getId());
            
            stmt.executeUpdate();
            return teacher;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update teacher: " + e.getMessage(), e);
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM teachers WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete teacher: " + e.getMessage(), e);
        }
    }

    // ✅ THIS IS THE METHOD YOUR DASHBOARD NEEDS
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM teachers";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Count query failed: " + e.getMessage(), e);
        }
    }

    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM teachers WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Count by status failed: " + e.getMessage(), e);
        }
    }

    // ======================== HELPERS ========================

    private Teacher mapResultSetToTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getInt("id"));
        t.setEmployeeId(rs.getString("employee_id"));
        t.setFirstName(rs.getString("first_name"));
        t.setLastName(rs.getString("last_name"));
        t.setEmail(rs.getString("email"));
        t.setPhone(rs.getString("phone"));
        t.setDateOfBirth(rs.getObject("date_of_birth", LocalDate.class));
        t.setGender(rs.getString("gender"));
        t.setAddress(rs.getString("address"));
        t.setSubjectSpecialization(rs.getString("subject_specialization"));
        t.setQualification(rs.getString("qualification"));
        t.setHireDate(rs.getObject("hire_date", LocalDate.class));
        t.setStatus(rs.getString("status"));
        t.setEmergencyContactName(rs.getString("emergency_contact_name"));
        t.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        return t;
    }

    private Optional<Teacher> executeQuerySingle(String sql, StatementSetter setter) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToTeacher(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private List<Teacher> executeQueryList(String sql, StatementSetter setter) {
        List<Teacher> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToTeacher(rs));
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