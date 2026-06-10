package com.example.appbanhang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.models.User;

public class AccountActivity extends AppCompatActivity {

    private Button btnEditProfile, btnLogout;
    private LinearLayout menuMyOrders, menuVoucher, menuShippingAddress, menuFaq, menuCustomerService, menuSettings;
    private TextView tvAccountName, tvAccountEmail, tvOrdersCount, tvDeliveredCount, tvReadyCount;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;

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

    @Override
    protected void onResume() {
        super.onResume();
        if (authManager != null && authManager.isLoggedIn()) {
            displayUserInfo();
        }
    }

    private void initializeViews() {
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        menuMyOrders = findViewById(R.id.menu_my_orders);
        menuVoucher = findViewById(R.id.menu_voucher);
        menuShippingAddress = findViewById(R.id.menu_shipping_address);
        menuFaq = findViewById(R.id.menu_faq);
        menuCustomerService = findViewById(R.id.menu_customer_service);
        menuSettings = findViewById(R.id.menu_settings);
        tvAccountName = findViewById(R.id.txt_account_name);
        tvAccountEmail = findViewById(R.id.txt_account_email);
        tvOrdersCount = findViewById(R.id.txt_orders_count);
        tvDeliveredCount = findViewById(R.id.txt_delivered_count);
        tvReadyCount = findViewById(R.id.txt_ready_count);
    }

    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
    }

    private void displayUserInfo() {
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        User currentUser = authManager.getCurrentUser();
        tvAccountName.setText(currentUser.getFullName());
        tvAccountEmail.setText(currentUser.getEmail());

        int orderCount = currentUser.getId() > 0 ? dbHelper.getOrderCount(currentUser.getId()) : 0;
        tvOrdersCount.setText(String.valueOf(orderCount));
        tvDeliveredCount.setText("0");
        tvReadyCount.setText(String.valueOf(orderCount));
    }

    private void setupListeners() {
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class)));

        menuMyOrders.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, OrderHistoryActivity.class)));
        menuVoucher.setOnClickListener(v -> openInfoScreen("voucher"));
        menuShippingAddress.setOnClickListener(v -> openInfoScreen("address"));
        menuFaq.setOnClickListener(v -> openInfoScreen("faq"));
        menuCustomerService.setOnClickListener(v -> openInfoScreen("support"));
        menuSettings.setOnClickListener(v -> openInfoScreen("settings"));
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class)));
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void openInfoScreen(String screen) {
        Intent intent = new Intent(AccountActivity.this, AccountInfoActivity.class);
        intent.putExtra("screen", screen);
        startActivity(intent);
    }

    private void handleLogout() {
        SharedPreferences.Editor editor = getSharedPreferences(SplashActivity.PREFS_NAME, MODE_PRIVATE).edit();
        editor.remove(SplashActivity.KEY_REMEMBERED_EMAIL);
        editor.apply();

        authManager.logout();

        Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
