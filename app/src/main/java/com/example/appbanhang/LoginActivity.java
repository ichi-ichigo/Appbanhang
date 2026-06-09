package com.example.appbanhang;

import android.content.Intent;
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
    }

    private void initializeDatabase() {
        dbHelper = new DatabaseHelper(this);
        AuthManager.initialize(dbHelper);
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
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        
        btnLoginGoogle.setOnClickListener(v -> {
            Toast.makeText(this, "Google Login - Chưa cài đặt", Toast.LENGTH_SHORT).show();
        });
        
        btnLoginFacebook.setOnClickListener(v -> {
            Toast.makeText(this, "Facebook Login - Chưa cài đặt", Toast.LENGTH_SHORT).show();
        });
        
        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Quên mật khẩu - Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
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
        if (authManager.login(email, password)) {
            Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
            
            // Navigate to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }
}
