package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.managers.AuthManager;

public class AccountActivity extends AppCompatActivity {

    private Button btnEditProfile;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeManagers();
        displayUserInfo();
        setupListeners();
    }

    private void initializeViews() {
        btnEditProfile = findViewById(R.id.btn_edit_profile);
    }

    private void initializeManagers() {
        authManager = AuthManager.getInstance();
    }

    private void displayUserInfo() {
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng chỉnh sửa hồ sơ - Đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }
}
