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