package dao;

import model.User;

import java.sql.*;

public class UserDAO {

    public void save(User u) {
        String sql = "INSERT INTO users (username, password, user_role, customer_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.username());
            ps.setString(2, u.passwordHash());
            ps.setString(3, u.role());
            if (u.customerId() == null) {
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(4, u.customerId());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save user", e);
        }
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cid = rs.getInt("customer_id");
                    Integer customerId = rs.wasNull() ? null : cid;
                    return new User(rs.getString("username"), rs.getString("password"),
                            rs.getString("user_role"), customerId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find user", e);
        }
        return null;
    }
}