package controller;

import dao.UserDAO;
import model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class LoginController {
    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        User u = userDAO.findByUsername(username);
        if (u == null || !u.passwordHash().equals(hash(password))) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return u;
    }

    public void createUser(String username, String password, String role, Integer customerId) {
        if (userDAO.findByUsername(username) != null) {
            return; // already exists
        }
        userDAO.save(new User(username, hash(password), role, customerId));
    }

    // a teller adds another teller
    public void addTeller(String username, String password) {
        String u = username == null ? "" : username.trim().toLowerCase();
        if (!u.matches("[a-z0-9]{3,20}")) {
            throw new IllegalArgumentException("Username must be 3-20 letters or digits");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userDAO.findByUsername(u) != null) {
            throw new IllegalArgumentException("That username is already taken");
        }
        userDAO.save(new User(u, hash(password), "TELLER", null));
    }

    // a teller resets the password of a teller or a customer
    public void resetPassword(String username, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userDAO.findByUsername(username) == null) {
            throw new IllegalArgumentException("No user with that username");
        }
        userDAO.updatePassword(username, hash(newPassword));
    }

    private static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}