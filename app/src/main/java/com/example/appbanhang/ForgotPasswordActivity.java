package com.example.appbanhang;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail, etNewPassword, etConfirmPassword;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager.initialize(dbHelper);
        authManager = AuthManager.getInstance();

        etEmail = findViewById(R.id.et_email);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_reset_password).setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (email.isEmpty() || !email.contains("@")) {
            Toast.makeText(this, "Email khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mat khau phai co it nhat 6 ky tu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mat khau nhap lai khong khop", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authManager.resetPassword(email, newPassword)) {
            Toast.makeText(this, "Da dat lai mat khau", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Email chua duoc dang ki", Toast.LENGTH_SHORT).show();
        }
    }
}
