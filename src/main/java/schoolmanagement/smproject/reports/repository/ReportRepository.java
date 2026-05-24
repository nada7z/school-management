package schoolmanagement.smproject.reports.repository;

import schoolmanagement.smproject.common.DatabaseConnection;
import schoolmanagement.smproject.reports.entity.Report;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    public Report save(Report report) {

        String sql = """
                INSERT INTO reports (
                    report_type,
                    report_title,
                    academic_year,
                    term,
                    report_date,
                    generated_by,
                    status,
                    summary_data,
                    file_path
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, report.getReportType());
            stmt.setString(2, report.getReportName());
            stmt.setString(3, report.getAcademicYear());
            stmt.setString(4, report.getTerm());
            stmt.setObject(5, report.getGeneratedDate());
            stmt.setString(6, report.getGeneratedBy());
            stmt.setString(7, report.getStatus());
            stmt.setString(8, report.getSummaryData());
            stmt.setString(9, report.getFilePath());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    report.setId(rs.getInt(1));
                }
            }

            return report;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save report: " + e.getMessage(), e);
        }
    }

    public List<Report> findAll() {

        String sql = "SELECT * FROM reports ORDER BY report_date DESC";

        return executeQueryList(sql, stmt -> {
        });
    }

    public List<Report> findByType(String reportType) {

        String sql = "SELECT * FROM reports WHERE report_type = ? ORDER BY report_date DESC";

        return executeQueryList(sql, stmt -> stmt.setString(1, reportType));
    }

    public List<Report> findByAcademicYear(String academicYear) {

        String sql = "SELECT * FROM reports WHERE academic_year = ? ORDER BY report_date DESC";

        return executeQueryList(sql, stmt -> stmt.setString(1, academicYear));
    }

    public boolean deleteById(int id) {

        String sql = "DELETE FROM reports WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete report: " + e.getMessage(), e);
        }
    }

    private Report mapResultSetToReport(ResultSet rs) throws SQLException {

        Report report = new Report();

        report.setId(rs.getInt("id"));
        report.setReportType(rs.getString("report_type"));
        report.setReportName(rs.getString("report_title"));
        report.setAcademicYear(rs.getString("academic_year"));
        report.setTerm(rs.getString("term"));

        report.setGeneratedDate(
                rs.getObject("report_date", LocalDate.class));

        report.setGeneratedBy(rs.getString("generated_by"));
        report.setStatus(rs.getString("status"));
        report.setSummaryData(rs.getString("summary_data"));
        report.setFilePath(rs.getString("file_path"));

        return report;
    }

    private List<Report> executeQueryList(String sql, StatementSetter setter) {

        List<Report> reports = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            setter.apply(stmt);

            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    reports.add(mapResultSetToReport(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }

        return reports;
    }

    @FunctionalInterface
    private interface StatementSetter {
        void apply(PreparedStatement stmt) throws SQLException;
    }
}