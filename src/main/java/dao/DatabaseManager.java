package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:h2:./data/bank";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initialise() {
        String customers = """
            CREATE TABLE IF NOT EXISTS customers (
                customer_id INT AUTO_INCREMENT PRIMARY KEY,
                first_name VARCHAR(50) NOT NULL,
                surname VARCHAR(50) NOT NULL,
                address VARCHAR(100) NOT NULL,
                phone VARCHAR(20),
                email VARCHAR(100),
                id_number VARCHAR(20),
                date_of_birth DATE
            )""";

        String accounts = """
            CREATE TABLE IF NOT EXISTS accounts (
                account_number VARCHAR(20) PRIMARY KEY,
                account_type VARCHAR(20) NOT NULL,
                branch VARCHAR(50) NOT NULL,
                balance DECIMAL(15,2) NOT NULL,
                customer_id INT NOT NULL,
                employer_name VARCHAR(100),
                employer_address VARCHAR(100),
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
            )""";

        String transactions = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id INT AUTO_INCREMENT PRIMARY KEY,
                account_number VARCHAR(20) NOT NULL,
                txn_type VARCHAR(20) NOT NULL,
                amount DECIMAL(15,2) NOT NULL,
                balance_after DECIMAL(15,2) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (account_number) REFERENCES accounts(account_number)
            )""";

        String users = """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(50) PRIMARY KEY,
                password VARCHAR(100) NOT NULL,
                user_role VARCHAR(20) NOT NULL,
                customer_id INT,
                FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
            )""";

        String[] upgrades = {
                "ALTER TABLE customers ADD COLUMN IF NOT EXISTS phone VARCHAR(20)",
                "ALTER TABLE customers ADD COLUMN IF NOT EXISTS email VARCHAR(100)",
                "ALTER TABLE customers ADD COLUMN IF NOT EXISTS id_number VARCHAR(20)",
                "ALTER TABLE customers ADD COLUMN IF NOT EXISTS date_of_birth DATE"
        };

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute(customers);
            st.execute(accounts);
            st.execute(transactions);
            st.execute(users);
            for (String sql : upgrades) {
                st.execute(sql);
            }
            System.out.println("Database ready.");
        } catch (SQLException e) {
            throw new RuntimeException("Could not set up database", e);
        }
    }
}