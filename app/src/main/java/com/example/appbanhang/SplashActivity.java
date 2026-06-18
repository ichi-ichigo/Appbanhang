package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

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

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (authManager.isLoggedIn()) {
                CartManager.getInstance().syncCart(new CartManager.CartSyncCallback() {
                    @Override
                    public void onSuccess() {
                        openScreen(MainActivity.class);
                    }

                    @Override
                    public void onError(String message) {
                        openScreen(MainActivity.class);
                    }
                });
                return;
            }

            openScreen(LoginActivity.class);
        }, 2000);
    }

    private void openScreen(Class<?> activityClass) {
        startActivity(new Intent(SplashActivity.this, activityClass));
        finish();
    }
}
