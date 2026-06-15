package schoolmanagement.smproject.attendance.repository;

import schoolmanagement.smproject.attendance.entity.Attendance;
import schoolmanagement.smproject.common.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttendanceRepository {

    public Attendance save(Attendance attendance) {
        // Uses MySQL's ON DUPLICATE KEY UPDATE to handle both Insert and Update
        // seamlessly
        String sql = "INSERT INTO attendance (person_type, person_id, person_name, class_name, date, status, remarks) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status = VALUES(status), remarks = VALUES(remarks)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            stmt.setString(i++, attendance.getPersonType());
            stmt.setInt(i++, attendance.getPersonId());
            stmt.setString(i++, attendance.getPersonName());
            stmt.setString(i++, attendance.getClassName());
            stmt.setObject(i++, attendance.getDate());
            stmt.setString(i++, attendance.getStatus());
            stmt.setString(i++, attendance.getRemarks());

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    attendance.setId(rs.getInt(1));
            }
            return attendance;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save attendance: " + e.getMessage(), e);
        }
    }

    public List<Attendance> findByDateAndType(LocalDate date, String personType) {
        String sql = "SELECT * FROM attendance WHERE date = ? AND person_type = ? ORDER BY person_name";
        return executeQueryList(sql, stmt -> {
            stmt.setObject(1, date);
            stmt.setString(2, personType);
        });
    }

    private Attendance mapResultSetToAttendance(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setId(rs.getInt("id"));
        a.setPersonType(rs.getString("person_type"));
        a.setPersonId(rs.getInt("person_id"));
        a.setPersonName(rs.getString("person_name"));
        a.setClassName(rs.getString("class_name"));
        a.setDate(rs.getObject("date", LocalDate.class));
        a.setStatus(rs.getString("status"));
        a.setRemarks(rs.getString("remarks"));
        return a;
    }

    private List<Attendance> executeQueryList(String sql, StatementSetter setter) {
        List<Attendance> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAttendance(rs));
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