package com.example.appbanhang;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.models.User;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        authManager = AuthManager.getInstance();
        etEmail = findViewById(R.id.et_email);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_reset_password).setOnClickListener(v -> resetPassword());
    }

    // Gui email dat lai mat khau.
    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        authManager.resetPassword(email, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Đã gửi liên kết đặt lại mật khẩu vào email của bạn.",
                        Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
