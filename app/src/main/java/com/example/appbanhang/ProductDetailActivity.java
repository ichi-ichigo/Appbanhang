package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.managers.CartManager;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvProductDescription;
    private Button btnBuyNow, btnFavorite;
    private CartManager cartManager;
    private int productId;
    private String productName;
    private double productPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize
        initializeViews();
        initializeManagers();
        loadProductData();
        setupListeners();
    }

    private void initializeViews() {
        tvProductName = findViewById(R.id.txt_product_name);
        tvProductPrice = findViewById(R.id.txt_product_price);
        tvProductDescription = findViewById(R.id.txt_product_description);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnFavorite = findViewById(R.id.btn_favorite);
    }

    private void initializeManagers() {
        cartManager = CartManager.getInstance();
    }

    private void loadProductData() {
        // Lấy dữ liệu từ Intent
        Intent intent = getIntent();
        productId = intent.getIntExtra("productId", 0);
        productName = intent.getStringExtra("productName");
        productPrice = intent.getDoubleExtra("productPrice", 0);
        String description = intent.getStringExtra("productDescription");

        // Hiển thị dữ liệu
        tvProductName.setText(productName);
        tvProductPrice.setText(String.format("Rp. %.0f", productPrice));
        tvProductDescription.setText(description);
    }

    private void setupListeners() {
        btnBuyNow.setOnClickListener(v -> handleBuyNow());
        btnFavorite.setOnClickListener(v -> handleFavorite());
    }

    private void handleBuyNow() {
        // Thêm sản phẩm vào giỏ hàng với số lượng mặc định
        cartManager.addToCart(productId, productName, productPrice, 1, "M");

        // Chuyển sang Cart Activity
        Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
        startActivity(intent);
    }

    private void handleFavorite() {
        Toast.makeText(this, "Thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        btnFavorite.setText("❤️");
    }
}
