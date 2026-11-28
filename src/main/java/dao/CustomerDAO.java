package dao;

import model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public void save(Customer c) {
        String sql = "INSERT INTO customers (first_name, surname, address, phone, email, "
                + "id_number, date_of_birth) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, c);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save customer", e);
        }
    }

    public Customer findById(int id) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find customer", e);
        }
        return null;
    }

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not list customers", e);
        }
        return customers;
    }

    public void update(Customer c) {
        String sql = "UPDATE customers SET first_name = ?, surname = ?, address = ?, phone = ?, "
                + "email = ?, id_number = ?, date_of_birth = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, c);
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not update customer", e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete customer", e);
        }
    }

    private void bind(PreparedStatement ps, Customer c) throws SQLException {
        ps.setString(1, c.getFirstName());
        ps.setString(2, c.getSurname());
        ps.setString(3, c.getAddress());
        ps.setString(4, c.getPhone());
        ps.setString(5, c.getEmail());
        ps.setString(6, c.getIdNumber());
        if (c.getDateOfBirth() == null) {
            ps.setNull(7, Types.DATE);
        } else {
            ps.setDate(7, Date.valueOf(c.getDateOfBirth()));
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Date dob = rs.getDate("date_of_birth");
        Customer c = new Customer(
                rs.getString("first_name"),
                rs.getString("surname"),
                rs.getString("address"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("id_number"),
                dob == null ? null : dob.toLocalDate());
        c.setId(rs.getInt("customer_id"));
        return c;
    }
}