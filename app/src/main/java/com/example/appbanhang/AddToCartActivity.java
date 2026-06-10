package com.example.appbanhang;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class AddToCartActivity extends AppCompatActivity {
    private ImageView imgProduct;
    private LinearLayout thumbnailContainer;
    private TextView txtProductName, txtProductPrice, txtQuantity;
    private Button btnMinus, btnPlus, btnAddToCart;
    private final List<Button> sizeButtons = new ArrayList<>();
    private Product product;
    private int quantity = 1;
    private String selectedSize = "41";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_cart);

        initializeManagers();
        initializeViews();
        loadProduct();
        setupListeners();
    }

    private void initializeManagers() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        CartManager.getInstance().syncFromDatabase();
    }

    private void initializeViews() {
        imgProduct = findViewById(R.id.img_product);
        thumbnailContainer = findViewById(R.id.thumbnail_container);
        txtProductName = findViewById(R.id.txt_product_name);
        txtProductPrice = findViewById(R.id.txt_product_price);
        txtQuantity = findViewById(R.id.txt_quantity);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        sizeButtons.add(findViewById(R.id.btn_size_39));
        sizeButtons.add(findViewById(R.id.btn_size_40));
        sizeButtons.add(findViewById(R.id.btn_size_41));
        sizeButtons.add(findViewById(R.id.btn_size_42));
        sizeButtons.add(findViewById(R.id.btn_size_43));
    }

    private void loadProduct() {
        int productId = getIntent().getIntExtra("product_id", 0);
        product = DataProvider.getProductById(productId);
        if (product == null) {
            Toast.makeText(this, "Khong tim thay san pham", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtProductName.setText(product.getName());
        txtProductPrice.setText(String.format("Rp. %.0f", product.getPrice()));
        ImageManager.getInstance().loadImageWithAnimation(product.getImageUrl(), imgProduct);
        setupGallery();
        updateQuantity();
        updateSizeButtons();
    }

    private void setupListeners() {
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            }
        });
        btnPlus.setOnClickListener(v -> {
            quantity++;
            updateQuantity();
        });
        btnAddToCart.setOnClickListener(v -> {
            if (product == null) {
                return;
            }
            CartManager.getInstance().addToCart(product, quantity, selectedSize);
            Toast.makeText(this, "Da them vao gio hang", Toast.LENGTH_SHORT).show();
            finish();
        });

        for (Button button : sizeButtons) {
            button.setOnClickListener(v -> {
                selectedSize = ((Button) v).getText().toString();
                updateSizeButtons();
            });
        }
    }

    private void setupGallery() {
        thumbnailContainer.removeAllViews();
        List<String> urls = product.getImageUrls();
        if (urls == null || urls.isEmpty()) {
            urls = new ArrayList<>();
            urls.add(product.getImageUrl());
        }

        for (String imageUrl : urls) {
            ImageView thumbnail = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(60), dpToPx(60));
            params.setMargins(0, 0, dpToPx(8), 0);
            thumbnail.setLayoutParams(params);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setBackgroundColor(0xFFE0E0E0);
            ImageManager.getInstance().loadThumbnail(imageUrl, thumbnail);
            thumbnail.setOnClickListener(v -> ImageManager.getInstance().loadImageWithAnimation(imageUrl, imgProduct));
            thumbnailContainer.addView(thumbnail);
        }
    }

    private void updateQuantity() {
        txtQuantity.setText(String.valueOf(quantity));
    }

    private void updateSizeButtons() {
        for (Button button : sizeButtons) {
            boolean selected = selectedSize.equals(button.getText().toString());
            button.setAlpha(selected ? 1.0f : 0.55f);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
