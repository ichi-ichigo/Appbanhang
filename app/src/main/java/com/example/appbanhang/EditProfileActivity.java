package com.example.appbanhang;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.models.User;

public class EditProfileActivity extends AppCompatActivity {
    private EditText etFullName, etPhone;
    private TextView txtEmail;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager.initialize(dbHelper);
        authManager = AuthManager.getInstance();

        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        txtEmail = findViewById(R.id.txt_email);

        bindUser();
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());
    }

    private void bindUser() {
        User user = authManager.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui long dang nhap", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etFullName.setText(user.getFullName());
        etPhone.setText(user.getPhoneNumber());
        txtEmail.setText(user.getEmail());
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui long nhap ho ten", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.length() < 10) {
            Toast.makeText(this, "So dien thoai phai co it nhat 10 so", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authManager.updateCurrentUserProfile(fullName, phone)) {
            Toast.makeText(this, "Da cap nhat ho so", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Khong the cap nhat ho so", Toast.LENGTH_SHORT).show();
        }
    }
}
