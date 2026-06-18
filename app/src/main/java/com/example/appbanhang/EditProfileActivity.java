package com.example.appbanhang;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.models.User;

public class EditProfileActivity extends AppCompatActivity {
    private EditText etFullName;
    private EditText etPhone;
    private TextView txtEmail;
    private ImageView imgAvatar;
    private Button btnChooseAvatar;
    private AuthManager authManager;
    private String avatarValue;

    private final ActivityResultLauncher<String[]> avatarPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    return;
                }
                persistAvatarPermission(uri);
                avatarValue = uri.toString();
                ImageManager.getInstance().loadAvatar(avatarValue, imgAvatar);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.edit_profile_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int horizontalPadding = dpToPx(20);
            v.setPadding(horizontalPadding, systemBars.top + dpToPx(12),
                    horizontalPadding, systemBars.bottom + dpToPx(20));
            return insets;
        });

        authManager = AuthManager.getInstance();
        etFullName = findViewById(R.id.et_full_name);
        etPhone = findViewById(R.id.et_phone);
        txtEmail = findViewById(R.id.txt_email);
        imgAvatar = findViewById(R.id.img_avatar);
        btnChooseAvatar = findViewById(R.id.btn_choose_avatar);

        bindUser();
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        imgAvatar.setOnClickListener(v -> openAvatarPicker());
        btnChooseAvatar.setOnClickListener(v -> openAvatarPicker());
        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile());
    }

    private void bindUser() {
        authManager.ensureCurrentUser(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                etFullName.setText(user.getFullName());
                etPhone.setText(user.getPhoneNumber());
                txtEmail.setText(user.getEmail());
                avatarValue = user.getAvatar();
                ImageManager.getInstance().loadAvatar(avatarValue, imgAvatar);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EditProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void openAvatarPicker() {
        avatarPickerLauncher.launch(new String[]{"image/*"});
    }

    private void persistAvatarPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException ignored) {
            // OpenDocument thường cho phép persist, nhưng nếu thiết bị không hỗ trợ thì vẫn giữ URI hiện tại.
        }
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone.length() < 10) {
            Toast.makeText(this, "Số điện thoại phải có ít nhất 10 số", Toast.LENGTH_SHORT).show();
            return;
        }

        authManager.updateCurrentUserProfile(fullName, phone, avatarValue, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(EditProfileActivity.this, "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(EditProfileActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
