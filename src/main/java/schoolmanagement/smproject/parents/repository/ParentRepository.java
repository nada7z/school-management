package schoolmanagement.smproject.parents.repository;

import schoolmanagement.smproject.parents.entity.Parent;
import schoolmanagement.smproject.common.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ParentRepository implements IParentRepository {

    @Override
    public Parent save(Parent parent) {
        String sql = """
            INSERT INTO parents (first_name, last_name, email, phone, phone_alternate, 
                                 relationship, occupation, address, is_primary_contact)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            mapParentToStatement(stmt, parent, false);
            int affected = stmt.executeUpdate();

            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        parent.setId(rs.getInt(1));
                    }
                }
            }
            return parent;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save parent: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Parent> findById(int id) {
        String sql = "SELECT * FROM parents WHERE id = ?";
        return executeQuerySingle(sql, stmt -> stmt.setInt(1, id));
    }

    @Override
    public Optional<Parent> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT * FROM parents WHERE LOWER(email) = LOWER(?)";
        return executeQuerySingle(sql, stmt -> stmt.setString(1, email.trim()));
    }

    @Override
    public Optional<Parent> findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return Optional.empty();
        String sql = "SELECT * FROM parents WHERE phone = ? OR phone_alternate = ?";
        return executeQuerySingle(sql, stmt -> {
            String cleaned = phone.trim();
            stmt.setString(1, cleaned);
            stmt.setString(2, cleaned);
        });
    }

    @Override
    public List<Parent> findAll() {
        String sql = "SELECT * FROM parents ORDER BY last_name, first_name";
        return executeQueryList(sql, stmt -> {});
    }

    @Override
    public List<Parent> findByLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) return List.of();
        String sql = "SELECT * FROM parents WHERE LOWER(last_name) LIKE LOWER(?) ORDER BY first_name";
        return executeQueryList(sql, stmt -> stmt.setString(1, "%" + lastName.trim() + "%"));
    }

    @Override
    public Parent update(Parent parent) {
        String sql = """
            UPDATE parents SET first_name = ?, last_name = ?, email = ?, phone = ?, 
                               phone_alternate = ?, relationship = ?, occupation = ?, 
                               address = ?, is_primary_contact = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            mapParentToStatement(stmt, parent, true);
            stmt.setInt(10, parent.getId());
            stmt.executeUpdate();
            return parent;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update parent: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM parents WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete parent: " + e.getMessage(), e);
        }
    }

    // 🔹 Helper: Map Parent object to PreparedStatement
    private void mapParentToStatement(PreparedStatement stmt, Parent parent, boolean includeId) {
        try {
            int index = 1;
            stmt.setString(index++, parent.getFirstName());
            stmt.setString(index++, parent.getLastName());
            stmt.setString(index++, parent.getEmail() != null ? parent.getEmail() : "");
            stmt.setString(index++, parent.getPhone());
            stmt.setString(index++, parent.getPhoneAlternate() != null ? parent.getPhoneAlternate() : "");
            stmt.setString(index++, parent.getRelationship() != null ? parent.getRelationship() : "");
            stmt.setString(index++, parent.getOccupation() != null ? parent.getOccupation() : "");
            stmt.setString(index++, parent.getAddress() != null ? parent.getAddress() : "");
            stmt.setBoolean(index++, parent.isPrimaryContact());
        } catch (SQLException e) {
            throw new RuntimeException("Statement mapping failed", e);
        }
    }

    // 🔹 Helper: Map ResultSet to Parent object
    private Parent mapResultSetToParent(ResultSet rs) throws SQLException {
        Parent parent = new Parent();
        parent.setId(rs.getInt("id"));
        parent.setFirstName(rs.getString("first_name"));
        parent.setLastName(rs.getString("last_name"));
        parent.setEmail(rs.getString("email"));
        parent.setPhone(rs.getString("phone"));
        parent.setPhoneAlternate(rs.getString("phone_alternate"));
        parent.setRelationship(rs.getString("relationship"));
        parent.setOccupation(rs.getString("occupation"));
        parent.setAddress(rs.getString("address"));
        parent.setPrimaryContact(rs.getBoolean("is_primary_contact"));
        return parent;
    }

    // 🔹 Helper: Execute query returning Optional<Parent>
    private Optional<Parent> executeQuerySingle(String sql, StatementSetter setter) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToParent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // 🔹 Helper: Execute query returning List<Parent>
    private List<Parent> executeQueryList(String sql, StatementSetter setter) {
        List<Parent> parents = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setter.apply(stmt);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    parents.add(mapResultSetToParent(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Query list failed: " + e.getMessage(), e);
        }
        return parents;
    }

    @FunctionalInterface
    private interface StatementSetter {
        void apply(PreparedStatement stmt) throws SQLException;
    }
}