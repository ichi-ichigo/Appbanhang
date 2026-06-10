package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.adapters.CartAdapter;
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.models.CartItem;

import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private EditText etPromoCode;
    private Button btnBack, btnCheckout, btnApplyPromo;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;
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
        etPromoCode = findViewById(R.id.et_promo_code);
        btnBack = findViewById(R.id.btn_back);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnApplyPromo = findViewById(R.id.btn_apply_promo);
    }

    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        cartManager = CartManager.getInstance();
        cartManager.syncFromDatabase();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerCart.setLayoutManager(layoutManager);

        List<CartItem> cartItems = cartManager.getAllItems();
        cartAdapter = new CartAdapter(cartItems, this);
        cartAdapter.setOnCartItemListener(new CartAdapter.OnCartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                cartManager.updateQuantity(item, newQuantity);
                updateTotals();
            }

            @Override
            public void onItemRemoved(CartItem item) {
                cartManager.removeFromCart(item);
                updateTotals();
            }
        });
        recyclerCart.setAdapter(cartAdapter);
    }

    private void updateTotals() {
        double subtotal = cartManager.getTotalPrice();
        double shipping = cartManager.isEmpty() ? 0 : cartManager.getShippingFee();
        double total = subtotal + shipping - cartManager.getDiscount();

        tvSubtotal.setText(String.format("Rp. %.0f", subtotal));
        tvShipping.setText(String.format("Rp. %.0f", shipping));
        tvTotal.setText(String.format("Rp. %.0f", total));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnApplyPromo.setOnClickListener(v -> applyPromoCode());

        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getAllItems().isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(CartActivity.this, BillingAddressActivity.class);
            startActivity(intent);
        });
    }

    private void applyPromoCode() {
        String code = etPromoCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã promo", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("SALE10".equals(code) || "GIAM10".equals(code)) {
            double discount = cartManager.getTotalPrice() * 0.1;
            cartManager.applyPromoCode(code, discount);
            updateTotals();
            Toast.makeText(this, "Đã áp dụng giảm 10%", Toast.LENGTH_SHORT).show();
        } else {
            cartManager.applyPromoCode(code, 0);
            updateTotals();
            Toast.makeText(this, "Mã promo không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
}
