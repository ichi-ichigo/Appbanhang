package com.example.appbanhang;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.models.User;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail, etNewPassword, etConfirmPassword;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();

        etEmail = findViewById(R.id.et_email);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_reset_password).setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi hàm reset mật khẩu của Firebase (gửi email)
        authManager.resetPassword(email, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(ForgotPasswordActivity.this, "Đã gửi link đặt lại mật khẩu vào Email của bạn!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
