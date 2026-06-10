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
        String normalizedEmail = normalizeEmail(email);

        // Check database first
        if (dbHelper != null && dbHelper.verifyUser(normalizedEmail, password)) {
            User user = dbHelper.getUserProfile(normalizedEmail);
            if (user != null) {
                currentUser = user;
            } else {
                currentUser = new User(normalizedEmail.split("@")[0], normalizedEmail, "");
            }
            return true;
        }

        // Fallback to in-memory database
        if (userDatabase.containsKey(normalizedEmail) &&
            userDatabase.get(normalizedEmail).equals(password)) {
            currentUser = new User();
            currentUser.setEmail(normalizedEmail);
            currentUser.setFullName(normalizedEmail.split("@")[0]);
            return true;
        }
        return false;
    }

    // Register
    public boolean register(String email, String password, String fullName) {
        return register(email, password, fullName, "");
    }

    public boolean register(String email, String password, String fullName, String phoneNumber) {
        String normalizedEmail = normalizeEmail(email);

        // Check if email already exists
        if (userDatabase.containsKey(normalizedEmail)) {
            return false; // Email đã tồn tại
        }

        if (dbHelper != null) {
            // Try to add to database
            boolean success = dbHelper.addUser(fullName, normalizedEmail, phoneNumber, password);
            if (success) {
                userDatabase.put(normalizedEmail, password); // Also add to in-memory for fallback
                User savedUser = dbHelper.getUserProfile(normalizedEmail);
                currentUser = savedUser != null ? savedUser : new User(fullName, normalizedEmail, phoneNumber);
                return true;
            }
            return false;
        }

        // Fallback to in-memory storage
        userDatabase.put(normalizedEmail, password);
        currentUser = new User(fullName, normalizedEmail, phoneNumber);
        return true;
    }

    // Logout
    public void logout() {
        currentUser = null;
    }

    public boolean restoreSession(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail.isEmpty() || dbHelper == null) {
            return false;
        }

        User user = dbHelper.getUserProfile(normalizedEmail);
        if (user == null) {
            return false;
        }

        currentUser = user;
        return true;
    }

    public boolean loginWithProvider(String providerName) {
        String provider = providerName == null ? "Social" : providerName.trim();
        if (provider.isEmpty()) {
            provider = "Social";
        }

        String normalizedProvider = provider.toLowerCase().replace(" ", "");
        String email = normalizedProvider + "@smarteshop.local";
        String fullName = provider + " User";

        if (dbHelper != null) {
            if (!dbHelper.getUserByEmail(email)) {
                dbHelper.addUser(fullName, email, "", "provider-login");
            }

            User user = dbHelper.getUserProfile(email);
            if (user != null) {
                currentUser = user;
                return true;
            }
        }

        currentUser = new User(fullName, email, "");
        return true;
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

    public boolean updateCurrentUserProfile(String fullName, String phoneNumber) {
        if (currentUser == null || dbHelper == null) {
            return false;
        }

        boolean updated = dbHelper.updateUserProfile(currentUser.getId(), fullName, phoneNumber);
        if (updated) {
            currentUser.setFullName(fullName);
            currentUser.setPhoneNumber(phoneNumber);
        }
        return updated;
    }

    // Change password
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        String normalizedEmail = normalizeEmail(email);

        if (userDatabase.containsKey(normalizedEmail) &&
            userDatabase.get(normalizedEmail).equals(oldPassword)) {
            userDatabase.put(normalizedEmail, newPassword);
            if (dbHelper != null) {
                dbHelper.addUserCredential(normalizedEmail, newPassword);
            }
            return true;
        }
        return false;
    }

    public boolean resetPassword(String email, String newPassword) {
        String normalizedEmail = normalizeEmail(email);
        if (dbHelper != null && dbHelper.getUserByEmail(normalizedEmail)) {
            userDatabase.put(normalizedEmail, newPassword);
            return dbHelper.updatePassword(normalizedEmail, newPassword);
        }

        if (userDatabase.containsKey(normalizedEmail)) {
            userDatabase.put(normalizedEmail, newPassword);
            return true;
        }
        return false;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
