package dao;

import model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CustomerDAO {

    public void save(Customer c) {
        String sql = "INSERT INTO customers (first_name, surname, address) VALUES (?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getFirstName());
                ps.setString(2, c.getSurname());
                ps.setString(3, c.getAddress());
                ps.executeUpdate();
                try (ResultSet Keys = ps.getGeneratedKeys()) {
                    if (Keys.next()) {
                        c.setId(Keys.getInt(1));
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
                ps.setInt(1,id);
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
        String sql = "SELECT * FROM customers";
        try (Connection conn = DatabaseManager.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        }catch (SQLException e) {
            throw new RuntimeException("Could not list customers", e);
        }
        return customers;
    }

    public void update(Customer c) {
        String sql = "UPDATE customers SET first_name = ?, surname = ? WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, c.getFirstName());
        ps.setString(2, c.getSurname());
        ps.setString(3, c.getAddress());
        ps.setInt(4, c.getId());
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

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer(
         rs.getString("first_name"),
         rs.getString("surname"),
         rs.getString("address"));
         c.setId(rs.getInt("customer_id"));
         return c;
        
    }
}
