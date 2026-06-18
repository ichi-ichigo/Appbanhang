package com.example.appbanhang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirebaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.models.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.Normalizer;
import java.util.Locale;

public class AccountActivity extends AppCompatActivity {

    private ImageView btnEditProfile;
    private Button btnLogout;
    private ImageButton btnBack;
    private LinearLayout menuMyOrders;
    private LinearLayout menuVoucher;
    private LinearLayout menuShippingAddress;
    private LinearLayout menuFaq;
    private LinearLayout menuCustomerService;
    private LinearLayout menuSettings;
    private TextView tvAccountName;
    private TextView tvAccountEmail;
    private TextView tvOrdersCount;
    private TextView tvDeliveredCount;
    private TextView tvReadyCount;
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
        btnBack = findViewById(R.id.btn_back);
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

        authManager.ensureCurrentUser(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User currentUser) {
                tvAccountName.setText(currentUser.getFullName());
                tvAccountEmail.setText(currentUser.getEmail());
                ImageManager.getInstance().loadAvatar(currentUser.getAvatar(), btnEditProfile);
                loadOrderStats(currentUser);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AccountActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, EditProfileActivity.class)));

        menuMyOrders.setOnClickListener(v ->
                startActivity(new Intent(AccountActivity.this, OrderHistoryActivity.class)));
        menuVoucher.setOnClickListener(v -> openInfoScreen("voucher"));
        menuShippingAddress.setOnClickListener(v -> openInfoScreen("address"));
        menuFaq.setOnClickListener(v -> openInfoScreen("faq"));
        menuCustomerService.setOnClickListener(v -> openInfoScreen("support"));
        menuSettings.setOnClickListener(v -> openInfoScreen("settings"));
        btnLogout.setOnClickListener(v -> handleLogout());
    }

    private void openInfoScreen(String screen) {
        Intent intent = new Intent(AccountActivity.this, AccountInfoActivity.class);
        intent.putExtra("screen", screen);
        startActivity(intent);
    }

    private void loadOrderStats(User currentUser) {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            showLocalOrderStats(currentUser);
            return;
        }

        tvOrdersCount.setText("...");
        tvDeliveredCount.setText("...");
        tvReadyCount.setText("...");

        FirebaseHelper.getFirestore()
                .collection("orders")
                .whereEqualTo("userUid", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = querySnapshot.size();
                    int delivered = 0;
                    int processing = 0;

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String status = getOrderStatus(document);
                        if (isDelivered(status)) {
                            delivered++;
                        } else if (!isCancelled(status)) {
                            processing++;
                        }
                    }

                    tvOrdersCount.setText(String.valueOf(total));
                    tvDeliveredCount.setText(String.valueOf(delivered));
                    tvReadyCount.setText(String.valueOf(processing));
                })
                .addOnFailureListener(error -> showLocalOrderStats(currentUser));
    }

    private void showLocalOrderStats(User currentUser) {
        int orderCount = currentUser != null && currentUser.getId() > 0
                ? dbHelper.getOrderCount(currentUser.getId())
                : 0;
        tvOrdersCount.setText(String.valueOf(orderCount));
        tvDeliveredCount.setText("0");
        tvReadyCount.setText(String.valueOf(orderCount));
    }

    private String getOrderStatus(DocumentSnapshot document) {
        String status = document.getString("orderStatus");
        return status == null || status.trim().isEmpty() ? "Dang xu ly" : status.trim();
    }

    private boolean isDelivered(String status) {
        String normalized = normalizeStatus(status);
        return normalized.contains("da giao") || normalized.contains("hoan thanh");
    }

    private boolean isCancelled(String status) {
        return normalizeStatus(status).contains("huy");
    }

    private String normalizeStatus(String status) {
        String value = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd');
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
