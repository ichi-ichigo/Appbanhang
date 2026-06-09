package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.managers.CartManager;

public class PaymentActivity extends AppCompatActivity {

    private RadioButton rbOnlineBanking, rbCard, rbPaypal;
    private TextView tvTotal;
    private Button btnPay, btnBack;
    private CartManager cartManager;

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
        tvTotal = findViewById(R.id.txt_total_payment);
        btnPay = findViewById(R.id.btn_place_order);
        btnBack = findViewById(R.id.btn_back);

        // Set default
        rbCard.setChecked(true);
    }

    private void initializeManagers() {
        cartManager = CartManager.getInstance();
    }

    private void displayTotal() {
        double total = cartManager.getTotalPrice() + 50000; // Including shipping
        tvTotal.setText(String.format("Rp. %.0f", total));
    }

    private void setupListeners() {
        btnPay.setOnClickListener(v -> handlePayment());
        btnBack.setOnClickListener(v -> finish());
    }

    private void handlePayment() {
        String paymentMethod = "Thẻ Tín Dụng";
        
        if (rbOnlineBanking.isChecked()) {
            paymentMethod = "Ngân Hàng Trực Tuyến";
        } else if (rbPaypal.isChecked()) {
            paymentMethod = "PayPal";
        }

        // Simulate payment processing
        Toast.makeText(this, "Xử lý thanh toán qua " + paymentMethod, Toast.LENGTH_SHORT).show();

        // Clear cart and go to success screen
        cartManager.clearCart();
        Intent intent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
        startActivity(intent);
        finish();
    }
}
