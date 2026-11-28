package dao;

import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public void save(Account a) {
        String sql = "INSERT INTO accounts (account_number, account_type, branch, balance, "
                + "customer_id, employer_name, employer_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAccountNumber());
            ps.setString(2, a.getClass().getSimpleName());
            ps.setString(3, a.getBranch());
            ps.setDouble(4, a.getBalance());
            ps.setInt(5, a.getOwner().getId());
            if (a instanceof ChequeAccount ch) {
                ps.setString(6, ch.getEmployerName());
                ps.setString(7, ch.getEmployerAddress());
            } else {
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save account", e);
        }
    }

    public List<Account> findByCustomer(Customer owner) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, owner.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRow(rs, owner));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load accounts", e);
        }
        return accounts;
    }

    public Account findByNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer owner = new CustomerDAO().findById(rs.getInt("customer_id"));
                    return mapRow(rs, owner);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find account", e);
        }
        return null;
    }

    public List<Account> findAll() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            CustomerDAO customerDAO = new CustomerDAO();
            while (rs.next()) {
                Customer owner = customerDAO.findById(rs.getInt("customer_id"));
                accounts.add(mapRow(rs, owner));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not load accounts", e);
        }
        return accounts;
    }

    public void updateBalance(Account a) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, a.getBalance());
            ps.setString(2, a.getAccountNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update balance", e);
        }
    }

    public void delete(String accountNumber) {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete account", e);
        }
    }

    private Account mapRow(ResultSet rs, Customer owner) throws SQLException {
        String type = rs.getString("account_type");
        String number = rs.getString("account_number");
        String branch = rs.getString("branch");
        double balance = rs.getDouble("balance");

        Account a;
        switch (type) {
            case "SavingsAccount" -> a = new SavingsAccount(number, branch, owner);
            case "InvestmentAccount" -> a = new InvestmentAccount(number, branch, owner, 500);
            case "ChequeAccount" -> a = new ChequeAccount(number, branch, owner,
                    rs.getString("employer_name"), rs.getString("employer_address"));
            default -> throw new SQLException("Unknown account type: " + type);
        }
        a.restoreBalance(balance);
        return a;
    }
}