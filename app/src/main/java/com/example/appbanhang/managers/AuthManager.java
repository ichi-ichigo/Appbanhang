package com.example.appbanhang.managers;

import com.example.appbanhang.firebase.FirebaseHelper;
import com.example.appbanhang.models.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.SetOptions;

import java.util.Locale;

public class AuthManager {
    private static AuthManager instance;
    private User currentUser;

    private AuthManager() {
    }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    public void login(String email, String password, AuthCallback callback) {
        String normalizedEmail = normalizeEmail(email);

        FirebaseHelper.getAuth().signInWithEmailAndPassword(normalizedEmail, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    fetchUserFromFirestore(uid, normalizedEmail, callback);
                })
                .addOnFailureListener(e ->
                        callback.onError("Dang nhap that bai: " + e.getMessage()));
    }

    public void register(String email, String password, String fullName, String phoneNumber, AuthCallback callback) {
        String normalizedEmail = normalizeEmail(email);

        FirebaseHelper.getAuth().createUserWithEmailAndPassword(normalizedEmail, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User newUser = new User(fullName, normalizedEmail, phoneNumber);
                    newUser.setId(buildStableUserId(uid));

                    FirebaseHelper.getFirestore()
                            .collection("users")
                            .document(uid)
                            .set(newUser)
                            .addOnSuccessListener(aVoid -> {
                                currentUser = newUser;
                                callback.onSuccess(newUser);
                            })
                            .addOnFailureListener(e ->
                                    callback.onError("Loi luu du lieu: " + e.getMessage()));
                })
                .addOnFailureListener(e ->
                        callback.onError("Dang ky that bai: " + e.getMessage()));
    }

    public void logout() {
        FirebaseHelper.getAuth().signOut();
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return FirebaseHelper.getAuth().getCurrentUser() != null;
    }

    private void fetchUserFromFirestore(String uid, String email, AuthCallback callback) {
        FirebaseHelper.getFirestore()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                    } else {
                        currentUser = new User(email.split("@")[0], email, "");
                    }

                    if (currentUser == null) {
                        currentUser = new User(email.split("@")[0], email, "");
                    }

                    currentUser.setId(buildStableUserId(uid));
                    if (currentUser.getEmail() == null || currentUser.getEmail().trim().isEmpty()) {
                        currentUser.setEmail(email);
                    }

                    callback.onSuccess(currentUser);
                })
                .addOnFailureListener(e ->
                        callback.onError("Loi lay thong tin: " + e.getMessage()));
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void ensureCurrentUser(AuthCallback callback) {
        if (currentUser != null) {
            callback.onSuccess(currentUser);
            return;
        }

        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("Vui long dang nhap");
            return;
        }

        String email = normalizeEmail(firebaseUser.getEmail());
        fetchUserFromFirestore(firebaseUser.getUid(), email, callback);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private int buildStableUserId(String uid) {
        int hash = uid == null ? 0 : uid.hashCode();
        return hash == Integer.MIN_VALUE ? Integer.MAX_VALUE : Math.abs(hash);
    }

    public void updateCurrentUserProfile(String fullName, String phoneNumber, AuthCallback callback) {
        updateCurrentUserProfile(fullName, phoneNumber,
                currentUser == null ? null : currentUser.getAvatar(), callback);
    }

    public void updateCurrentUserProfile(String fullName, String phoneNumber, String avatar, AuthCallback callback) {
        if (currentUser == null) {
            callback.onError("Vui long dang nhap");
            return;
        }

        currentUser.setFullName(fullName);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setAvatar(avatar);

        FirebaseHelper.getFirestore()
                .collection("users")
                .document(FirebaseHelper.getAuth().getUid())
                .set(currentUser, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess(currentUser))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void resetPassword(String email, AuthCallback callback) {
        FirebaseHelper.getAuth().sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void loginWithProvider(String provider) {
    }
}
