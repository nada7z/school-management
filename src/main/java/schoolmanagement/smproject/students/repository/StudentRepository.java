package schoolmanagement.smproject.students.repository;

import schoolmanagement.smproject.parents.entity.Parent;
import schoolmanagement.smproject.students.entity.Student;
import schoolmanagement.smproject.common.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.stage.Stage;

public class StudentRepository implements IStudentRepository {

    @Override
    public Student save(Student student) {
        String sql = """
            INSERT INTO students (first_name, last_name, email, phone, date_of_birth, gender, 
                                  address, enrollment_date, grade_level, status,
                                  primary_parent_id, secondary_parent_id,
                                  emergency_contact_name, emergency_contact_phone, emergency_contact_relationship)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            mapStudentToStatement(stmt, student, false);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) student.setId(rs.getInt(1));
            }
            return student;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save student: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Student> findById(int id) {
        // Uses LEFT JOIN to fetch full parent data in one query
        String sql = """
            SELECT s.*, 
                   p1.id AS p1_id, p1.first_name AS p1_fn, p1.last_name AS p1_ln, p1.email AS p1_em, p1.phone AS p1_ph, p1.phone_alternate AS p1_pa, p1.relationship AS p1_rel, p1.occupation AS p1_occ, p1.address AS p1_addr, p1.is_primary_contact AS p1_ip,
                   p2.id AS p2_id, p2.first_name AS p2_fn, p2.last_name AS p2_ln, p2.email AS p2_em, p2.phone AS p2_ph, p2.phone_alternate AS p2_pa, p2.relationship AS p2_rel, p2.occupation AS p2_occ, p2.address AS p2_addr, p2.is_primary_contact AS p2_ip
            FROM students s
            LEFT JOIN parents p1 ON s.primary_parent_id = p1.id
            LEFT JOIN parents p2 ON s.secondary_parent_id = p2.id
            WHERE s.id = ?
            """;
        return executeQuerySingle(sql, stmt -> stmt.setInt(1, id), true);
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT * FROM students WHERE LOWER(email) = LOWER(?)";
        return executeQuerySingle(sql, stmt -> stmt.setString(1, email.trim()), false);
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT * FROM students ORDER BY last_name, first_name";
        return executeQueryList(sql, stmt -> {}, false);
    }

    @Override
    public List<Student> findByGradeLevel(String gradeLevel) {
        if (gradeLevel == null || gradeLevel.trim().isEmpty()) return List.of();
        String sql = "SELECT * FROM students WHERE grade_level = ? ORDER BY last_name";
        return executeQueryList(sql, stmt -> stmt.setString(1, gradeLevel.trim()), false);
    }

    @Override
    public List<Student> findByStatus(String status) {
        if (status == null || status.trim().isEmpty()) return List.of();
        String sql = "SELECT * FROM students WHERE status = ? ORDER BY last_name";
        return executeQueryList(sql, stmt -> stmt.setString(1, status.trim()), false);
    }

    @Override
    public Student update(Student student) {
        String sql = """
            UPDATE students SET first_name = ?, last_name = ?, email = ?, phone = ?, 
                                date_of_birth = ?, gender = ?, address = ?, 
                                enrollment_date = ?, grade_level = ?, status = ?,
                                primary_parent_id = ?, secondary_parent_id = ?,
                                emergency_contact_name = ?, emergency_contact_phone = ?, emergency_contact_relationship = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            mapStudentToStatement(stmt, student, true);
            stmt.setInt(16, student.getId());
            stmt.executeUpdate();
            return student;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update student: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete student: " + e.getMessage(), e);
        }
    }

    // 📊 Dashboard Statistics
    @Override
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM students WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Count query failed: " + e.getMessage(), e);
        }
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM students";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Count all query failed: " + e.getMessage(), e);
        }
    }

    // 🔹 Helper: Map Student object to PreparedStatement
    private void mapStudentToStatement(PreparedStatement stmt, Student student, boolean includeId) {
        try {
            int i = 1;
            stmt.setString(i++, student.getFirstName());
            stmt.setString(i++, student.getLastName());
            stmt.setString(i++, student.getEmail() != null ? student.getEmail() : "");
            stmt.setString(i++, student.getPhone() != null ? student.getPhone() : "");
            stmt.setObject(i++, student.getDateOfBirth()); // LocalDate
            stmt.setString(i++, student.getGender());
            stmt.setString(i++, student.getAddress() != null ? student.getAddress() : "");
            stmt.setObject(i++, student.getEnrollmentDate()); // LocalDate
            stmt.setString(i++, student.getGradeLevel());
            stmt.setString(i++, student.getStatus() != null ? student.getStatus() : "Active");
            
            // Parent IDs (handle nulls safely)
            stmt.setObject(i++, student.getPrimaryParent() != null ? student.getPrimaryParent().getId() : null);
            stmt.setObject(i++, student.getSecondaryParent() != null ? student.getSecondaryParent().getId() : null);
            
            stmt.setString(i++, student.getEmergencyContactName() != null ? student.getEmergencyContactName() : "");
            stmt.setString(i++, student.getEmergencyContactPhone() != null ? student.getEmergencyContactPhone() : "");
            stmt.setString(i++, student.getEmergencyContactRelationship() != null ? student.getEmergencyContactRelationship() : "");
        } catch (SQLException e) {
            throw new RuntimeException("Statement mapping failed", e);
        }
    }

    // 🔹 Helper: Map ResultSet to Student object
    private Student mapResultSetToStudent(ResultSet rs, boolean joinParents) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDateOfBirth(rs.getObject("date_of_birth", LocalDate.class));
        s.setGender(rs.getString("gender"));
        s.setAddress(rs.getString("address"));
        s.setEnrollmentDate(rs.getObject("enrollment_date", LocalDate.class));
        s.setGradeLevel(rs.getString("grade_level"));
        s.setStatus(rs.getString("status"));
        s.setEmergencyContactName(rs.getString("emergency_contact_name"));
        s.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        s.setEmergencyContactRelationship(rs.getString("emergency_contact_relationship"));

        if (joinParents) {
            s.setPrimaryParent(mapParent(rs, "p1_"));
            s.setSecondaryParent(mapParent(rs, "p2_"));
        }
        return s;
    }

    // 🔹 Helper: Extract Parent from prefixed columns (p1_, p2_)
    private Parent mapParent(ResultSet rs, String prefix) throws SQLException {
        int id = rs.getInt(prefix + "id");
        if (rs.wasNull()) return null; // No parent linked
        
        Parent p = new Parent();
        p.setId(id);
        p.setFirstName(rs.getString(prefix + "fn"));
        p.setLastName(rs.getString(prefix + "ln"));
        p.setEmail(rs.getString(prefix + "em"));
        p.setPhone(rs.getString(prefix + "ph"));
        p.setPhoneAlternate(rs.getString(prefix + "pa"));
        p.setRelationship(rs.getString(prefix + "rel"));
        p.setOccupation(rs.getString(prefix + "occ"));
        p.setAddress(rs.getString(prefix + "addr"));
        p.setPrimaryContact(rs.getBoolean(prefix + "ip"));
        return p;
    }

    // 🔹 Execute query returning Optional<Student>
    private Optional<Student> executeQuerySingle(String sql, StatementSetter setter, boolean joinParents) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToStudent(rs, joinParents));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // 🔹 Execute query returning List<Student>
    private List<Student> executeQueryList(String sql, StatementSetter setter, boolean joinParents) {
        List<Student> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToStudent(rs, joinParents));
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

    private Stage dashboardStage;

    public void setDashboardStage(Stage stage) {
    this.dashboardStage = stage;
    }

    public List<Student> findByClassroomId(int classroomId) {
    String sql = """
        SELECT *
        FROM students
        WHERE classroom_id = ?
        ORDER BY last_name, first_name
        """;

    return executeQueryList(sql, stmt -> stmt.setInt(1, classroomId), false);
    }
}