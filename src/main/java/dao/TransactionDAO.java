package dao;

import model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void record(String accountNumber, String type, double amount, double balanceAfter) {
        String sql = "INSERT INTO transactions (account_number, txn_type, amount, balance_after) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, type);
            ps.setDouble(3, amount);
            ps.setDouble(4, balanceAfter);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not record transaction", e);
        }
    }

    public List<Transaction> findByAccount(String accountNumber) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY transaction_id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getString("account_number"),
                            rs.getString("txn_type"),
                            rs.getDouble("amount"),
                            rs.getDouble("balance_after"),
                            rs.getTimestamp("created_at").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load transactions", e);
        }
        return list;
    }

    public void deleteByAccount(String accountNumber) {
        String sql = "DELETE FROM transactions WHERE account_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete transactions", e);
        }
    }
}