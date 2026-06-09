package com.example.appbanhang.managers;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.models.User;
import java.util.HashMap;
import java.util.Map;

public class AuthManager {
    private static AuthManager instance;
    private User currentUser;
    private Map<String, String> userDatabase; // Email -> Password (fallback in memory)
    private static DatabaseHelper dbHelper; // For persistent storage

    private AuthManager() {
        this.userDatabase = new HashMap<>();
        // Tài khoản mẫu (fallback)
        userDatabase.put("sultan@example.com", "password123");
        userDatabase.put("user@example.com", "user123");
    }

    // Initialize with DatabaseHelper
    public static void initialize(DatabaseHelper helper) {
        dbHelper = helper;
    }

    // Singleton Pattern
    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    // Login
    public boolean login(String email, String password) {
        // Check database first
        if (dbHelper != null && dbHelper.verifyUser(email, password)) {
            currentUser = new User();
            currentUser.setEmail(email);
            currentUser.setFullName(email.split("@")[0]);
            return true;
        }

        // Fallback to in-memory database
        if (userDatabase.containsKey(email) && 
            userDatabase.get(email).equals(password)) {
            currentUser = new User();
            currentUser.setEmail(email);
            currentUser.setFullName(email.split("@")[0]);
            return true;
        }
        return false;
    }

    // Register
    public boolean register(String email, String password, String fullName) {
        // Check if email already exists
        if (userDatabase.containsKey(email)) {
            return false; // Email đã tồn tại
        }

        if (dbHelper != null) {
            // Try to add to database
            boolean success = dbHelper.addUser(fullName, email, "", password);
            if (success) {
                userDatabase.put(email, password); // Also add to in-memory for fallback
                currentUser = new User(fullName, email, "");
                return true;
            }
            return false;
        }

        // Fallback to in-memory storage
        userDatabase.put(email, password);
        currentUser = new User(fullName, email, "");
        return true;
    }

    // Logout
    public void logout() {
        currentUser = null;
    }

    // Get current user
    public User getCurrentUser() {
        return currentUser;
    }

    // Check if logged in
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Update user profile
    public void updateUserProfile(User user) {
        this.currentUser = user;
    }

    // Change password
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        if (userDatabase.containsKey(email) && 
            userDatabase.get(email).equals(oldPassword)) {
            userDatabase.put(email, newPassword);
            if (dbHelper != null) {
                dbHelper.addUserCredential(email, newPassword);
            }
            return true;
        }
        return false;
    }
}
