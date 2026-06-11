package com.example.appbanhang.managers;

import com.example.appbanhang.firebase.FirebaseHelper;
import com.example.appbanhang.models.User;
import com.google.firebase.auth.FirebaseUser;

public class AuthManager {
    private static AuthManager instance;
    private User currentUser;

    private AuthManager() {
        // Đã xóa bỏ HashMap userDatabase và DatabaseHelper cũ
    }

    // Singleton Pattern
    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    // TẠO INTERFACE LẮNG NGHE KẾT QUẢ TỪ FIREBASE
    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }

    // 1. SỬA HÀM LOGIN
    public void login(String email, String password, AuthCallback callback) {
        String normalizedEmail = normalizeEmail(email);

        FirebaseHelper.getAuth().signInWithEmailAndPassword(normalizedEmail, password)
            .addOnSuccessListener(authResult -> {
                // Đăng nhập Auth thành công, lấy thông tin User từ Firestore
                String uid = authResult.getUser().getUid();
                fetchUserFromFirestore(uid, normalizedEmail, callback);
            })
            .addOnFailureListener(e -> {
                callback.onError("Đăng nhập thất bại: " + e.getMessage());
            });
    }

    // 2. SỬA HÀM REGISTER
    public void register(String email, String password, String fullName, String phoneNumber, AuthCallback callback) {
        String normalizedEmail = normalizeEmail(email);

        // Tạo tài khoản trên Firebase Auth
        FirebaseHelper.getAuth().createUserWithEmailAndPassword(normalizedEmail, password)
            .addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();
                
                // Tạo đối tượng User để lưu thêm thông tin (Tên, SDT) lên Firestore
                User newUser = new User(fullName, normalizedEmail, phoneNumber);
                
                // Lưu vào Collection "users"
                FirebaseHelper.getFirestore().collection("users").document(uid)
                    .set(newUser)
                    .addOnSuccessListener(aVoid -> {
                        this.currentUser = newUser;
                        callback.onSuccess(newUser);
                    })
                    .addOnFailureListener(e -> callback.onError("Lỗi lưu dữ liệu: " + e.getMessage()));
            })
            .addOnFailureListener(e -> callback.onError("Đăng ký thất bại: " + e.getMessage()));
    }

    // 3. SỬA HÀM LOGOUT
    public void logout() {
        FirebaseHelper.getAuth().signOut(); // Đăng xuất khỏi Firebase
        currentUser = null;
    }

    // 4. KIỂM TRA ĐĂNG NHẬP
    public boolean isLoggedIn() {
        return FirebaseHelper.getAuth().getCurrentUser() != null;
    }

    // Hàm phụ trợ: Lấy dữ liệu User từ Firestore
    private void fetchUserFromFirestore(String uid, String email, AuthCallback callback) {
        FirebaseHelper.getFirestore().collection("users").document(uid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUser = documentSnapshot.toObject(User.class);
                } else {
                    // Nếu tài khoản chưa có dữ liệu profile trên Firestore thì tạo tạm
                    currentUser = new User(email.split("@")[0], email, "");
                }
                callback.onSuccess(currentUser);
            })
            .addOnFailureListener(e -> callback.onError("Lỗi lấy thông tin: " + e.getMessage()));
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
    // Cập nhật Profile lên Firebase
    public void updateCurrentUserProfile(String fullName, String phoneNumber, AuthCallback callback) {
        if (currentUser == null) return;
        currentUser.setFullName(fullName);
        currentUser.setPhoneNumber(phoneNumber);
        
        FirebaseHelper.getFirestore().collection("users").document(FirebaseHelper.getAuth().getUid())
            .set(currentUser)
            .addOnSuccessListener(aVoid -> callback.onSuccess(currentUser))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Quên mật khẩu (Gửi email reset)
    public void resetPassword(String email, AuthCallback callback) {
        FirebaseHelper.getAuth().sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Đăng nhập bằng Google/Facebook (Tạm thời để trống, cấu hình sau)
    public void loginWithProvider(String provider) {
        // Sẽ code Firebase Social Login vào đây sau
    }
}