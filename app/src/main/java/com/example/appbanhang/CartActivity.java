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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.adapters.CartAdapter;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.models.CartItem;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private Button btnCheckout, btnContinueShopping;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize
        initializeViews();
        initializeManagers();
        setupRecyclerView();
        updateTotals();
        setupListeners();
    }

    private void initializeViews() {
        recyclerCart = findViewById(R.id.recycler_cart_items);
        tvSubtotal = findViewById(R.id.txt_subtotal);
        tvShipping = findViewById(R.id.txt_shipping_fee);
        tvTotal = findViewById(R.id.txt_total);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnContinueShopping = findViewById(R.id.btn_checkout);
    }

    private void initializeManagers() {
        cartManager = CartManager.getInstance();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerCart.setLayoutManager(layoutManager);

        List<CartItem> cartItems = cartManager.getAllItems();
        cartAdapter = new CartAdapter(cartItems, this);
        recyclerCart.setAdapter(cartAdapter);
    }

    private void updateTotals() {
        double subtotal = cartManager.getTotalPrice();
        double shipping = 50000; // Shipping fee mẫu
        double total = subtotal + shipping;

        tvSubtotal.setText(String.format("Rp. %.0f", subtotal));
        tvShipping.setText(String.format("Rp. %.0f", shipping));
        tvTotal.setText(String.format("Rp. %.0f", total));
    }

    private void setupListeners() {
        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getAllItems().isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(CartActivity.this, BillingAddressActivity.class);
            startActivity(intent);
        });

        btnContinueShopping.setOnClickListener(v -> {
            finish();
        });
    }
}
