package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProductMain;
    private LinearLayout thumbnailContainer;
    private TextView tvProductName, tvProductCategory, tvProductPrice, tvProductDescription;
    private Button btnBack, btnBuyNow, btnFavorite;
    private CartManager cartManager;
    private WishlistManager wishlistManager;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;
    private Product product;

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
        imgProductMain = findViewById(R.id.img_product_main);
        thumbnailContainer = findViewById(R.id.thumbnail_container);
        tvProductName = findViewById(R.id.txt_product_name);
        tvProductCategory = findViewById(R.id.txt_product_category);
        tvProductPrice = findViewById(R.id.txt_product_price);
        tvProductDescription = findViewById(R.id.txt_product_description);
        btnBack = findViewById(R.id.btn_back);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnFavorite = findViewById(R.id.btn_favorite);
    }

    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        WishlistManager.initialize(dbHelper, authManager);
        cartManager = CartManager.getInstance();
        wishlistManager = WishlistManager.getInstance();
    }

    private void loadProductData() {
        Intent intent = getIntent();
        int productId = intent.getIntExtra("product_id", intent.getIntExtra("productId", 0));
        product = DataProvider.getProductById(productId);

        if (product == null && productId != 0) {
            product = new Product(
                    productId,
                    getStringExtraOrDefault(intent, "productName", "Sản phẩm"),
                    getStringExtraOrDefault(intent, "productCategory", ""),
                    intent.getDoubleExtra("productPrice", 0),
                    getStringExtraOrDefault(intent, "productImage", ""),
                    getStringExtraOrDefault(intent, "productDescription", ""),
                    intent.getDoubleExtra("productRating", 0),
                    getStringExtraOrDefault(intent, "productBrand", "")
            );
        }

        if (product == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        product.setFavorite(wishlistManager.isInWishlist(product.getId()));
        tvProductName.setText(product.getName());
        tvProductCategory.setText(product.getCategory());
        tvProductPrice.setText(String.format("Rp. %.0f", product.getPrice()));
        tvProductDescription.setText(product.getDescription());
        updateFavoriteButton();
        setupGallery();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnBuyNow.setOnClickListener(v -> handleBuyNow());
        btnFavorite.setOnClickListener(v -> handleFavorite());
    }

    private void handleBuyNow() {
        if (product == null) {
            return;
        }

        Intent intent = new Intent(ProductDetailActivity.this, AddToCartActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    private void handleFavorite() {
        if (product == null) {
            return;
        }

        if (wishlistManager.isInWishlist(product.getId())) {
            wishlistManager.removeFromWishlistById(product.getId());
            product.setFavorite(false);
            Toast.makeText(this, "Đã bỏ khỏi yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            wishlistManager.addToWishlist(product);
            product.setFavorite(true);
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        }
        updateFavoriteButton();
    }

    private void setupGallery() {
        List<String> imageUrls = new ArrayList<>();
        if (product.getImageUrls() != null) {
            imageUrls.addAll(product.getImageUrls());
        }
        if (imageUrls.isEmpty() && product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            imageUrls.add(product.getImageUrl());
        }

        if (imageUrls.isEmpty()) {
            imgProductMain.setImageResource(R.drawable.ic_launcher_foreground);
            thumbnailContainer.removeAllViews();
            return;
        }

        ImageManager.getInstance().loadImageWithAnimation(imageUrls.get(0), imgProductMain);
        thumbnailContainer.removeAllViews();

        for (String imageUrl : imageUrls) {
            ImageView thumbnail = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(70), dpToPx(70));
            params.setMargins(0, 0, dpToPx(8), 0);
            thumbnail.setLayoutParams(params);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setContentDescription(getString(R.string.product_thumbnail));
            thumbnail.setBackgroundColor(0xFFEFEFEF);
            thumbnail.setPadding(dpToPx(2), dpToPx(2), dpToPx(2), dpToPx(2));
            ImageManager.getInstance().loadThumbnail(imageUrl, thumbnail);
            thumbnail.setOnClickListener(v ->
                    ImageManager.getInstance().loadImageWithAnimation(imageUrl, imgProductMain));
            thumbnailContainer.addView(thumbnail);
        }
    }

    private void updateFavoriteButton() {
        btnFavorite.setText(product != null && product.isFavorite() ? "❤️" : "🤍");
    }

    private String getStringExtraOrDefault(Intent intent, String key, String defaultValue) {
        String value = intent.getStringExtra(key);
        return value == null ? defaultValue : value;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
