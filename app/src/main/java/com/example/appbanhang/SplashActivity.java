package com.example.appbanhang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.WishlistManager;

public class SplashActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "smarteshop_prefs";
    public static final String KEY_REMEMBERED_EMAIL = "remembered_email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        WishlistManager.initialize(dbHelper, authManager);

        new Handler().postDelayed(() -> {
            Intent intent;
            // Thay đổi lớn: Check bằng Firebase isLoggedIn
            if (authManager.isLoggedIn()) {
                CartManager.getInstance().syncFromDatabase();
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2000);
    }
}
