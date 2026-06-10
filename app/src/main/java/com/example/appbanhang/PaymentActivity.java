package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.models.User;

public class PaymentActivity extends AppCompatActivity {

    private RadioButton rbOnlineBanking, rbCard, rbPaypal;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private Button btnPay, btnBack;
    private CartManager cartManager;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeManagers();
        displayTotal();
        setupListeners();
    }

    private void initializeViews() {
        rbOnlineBanking = findViewById(R.id.radio_online_banking);
        rbCard = findViewById(R.id.radio_card);
        rbPaypal = findViewById(R.id.radio_paypal);
        tvSubtotal = findViewById(R.id.txt_subtotal);
        tvShipping = findViewById(R.id.txt_shipping);
        tvTotal = findViewById(R.id.txt_total_payment);
        btnPay = findViewById(R.id.btn_place_order);
        btnBack = findViewById(R.id.btn_back);

        selectPaymentMethod(rbOnlineBanking);
    }

    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        cartManager = CartManager.getInstance();
        cartManager.syncFromDatabase();
    }

    private void displayTotal() {
        double subtotal = cartManager.getTotalPrice();
        double shipping = cartManager.isEmpty() ? 0 : cartManager.getShippingFee();
        double total = subtotal + shipping - cartManager.getDiscount();
        tvSubtotal.setText(String.format("Rp. %.0f", subtotal));
        tvShipping.setText(String.format("Rp. %.0f", shipping));
        tvTotal.setText(String.format("Rp. %.0f", total));
    }

    private void setupListeners() {
        btnPay.setOnClickListener(v -> handlePayment());
        btnBack.setOnClickListener(v -> finish());

        rbOnlineBanking.setOnClickListener(v -> selectPaymentMethod(rbOnlineBanking));
        rbCard.setOnClickListener(v -> selectPaymentMethod(rbCard));
        rbPaypal.setOnClickListener(v -> selectPaymentMethod(rbPaypal));
    }

    private void handlePayment() {
        if (cartManager.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String paymentMethod = "Thẻ Tín Dụng";
        
        if (rbOnlineBanking.isChecked()) {
            paymentMethod = "Ngân Hàng Trực Tuyến";
        } else if (rbPaypal.isChecked()) {
            paymentMethod = "PayPal";
        }

        // Simulate payment processing
        Toast.makeText(this, "Xử lý thanh toán qua " + paymentMethod, Toast.LENGTH_SHORT).show();

        User currentUser = authManager.getCurrentUser();
        int userId = currentUser == null ? 0 : currentUser.getId();
        double total = cartManager.getTotalPrice() + cartManager.getShippingFee() - cartManager.getDiscount();
        String deliveryAddress = getIntent().getStringExtra("delivery_address");
        dbHelper.addOrder(userId, total, paymentMethod, "Pending",
                deliveryAddress == null ? "" : deliveryAddress);

        cartManager.clearCart();
        Intent intent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
        startActivity(intent);
        finish();
    }

    private void selectPaymentMethod(RadioButton selected) {
        rbOnlineBanking.setChecked(selected == rbOnlineBanking);
        rbCard.setChecked(selected == rbCard);
        rbPaypal.setChecked(selected == rbPaypal);
    }
}
