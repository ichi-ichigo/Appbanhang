package com.example.appbanhang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnLoginGoogle, btnLoginFacebook;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword, tvSignUp;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Apply window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize
        initializeDatabase();
        initializeViews();
        initializeManager();
        setupListeners();
        setupCompletedListeners();
    }

    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etLoginEmail);
        etPassword = findViewById(R.id.etLoginPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        btnLoginFacebook = findViewById(R.id.btnLoginFacebook);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    private void initializeManager() {
        authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        WishlistManager.initialize(dbHelper, authManager);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        
        btnLoginGoogle.setOnClickListener(v -> {
            handleProviderLogin("Google");
        });
        
        btnLoginFacebook.setOnClickListener(v -> {
            handleProviderLogin("Facebook");
        });
        
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
        
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void setupCompletedListeners() {
        btnLoginGoogle.setOnClickListener(v -> handleProviderLogin("Google"));
        btnLoginFacebook.setOnClickListener(v -> handleProviderLogin("Facebook"));
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Attempt login
        authManager.login(email, password, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                saveRememberedLogin(email);
                CartManager.getInstance().syncFromDatabase(); // Giữ nguyên, sửa Cart sau
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleProviderLogin(String provider) {
        // Tạm thời thông báo chưa hỗ trợ để app không bị lỗi
        Toast.makeText(this, "Tính năng đăng nhập bằng " + provider + " đang được cập nhật sang Firebase!", Toast.LENGTH_SHORT).show();
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void saveRememberedLogin(String email) {
        SharedPreferences.Editor editor = getSharedPreferences(SplashActivity.PREFS_NAME, MODE_PRIVATE).edit();
        if (cbRememberMe.isChecked()) {
            editor.putString(SplashActivity.KEY_REMEMBERED_EMAIL, email.trim().toLowerCase());
        } else {
            editor.remove(SplashActivity.KEY_REMEMBERED_EMAIL);
        }
        editor.apply();
    }
}
