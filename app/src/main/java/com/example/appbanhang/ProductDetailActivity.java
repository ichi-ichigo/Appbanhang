package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.ProductDisplayUtils;

import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProductMain;
    private TextView tvProductName, tvProductCategory, tvProductPrice, tvProductDescription;
    private ImageButton btnBack, btnFavorite;
    private Button btnBuyNow;
    private WishlistManager wishlistManager;
    private FirestoreRepository firestoreRepository;
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

        initializeViews();
        initializeManagers();
        setupListeners();
        loadProductData();
    }

    private void initializeViews() {
        imgProductMain = findViewById(R.id.img_product_main);
        tvProductName = findViewById(R.id.txt_product_name);
        tvProductCategory = findViewById(R.id.txt_product_category);
        tvProductPrice = findViewById(R.id.txt_product_price);
        tvProductDescription = findViewById(R.id.txt_product_description);
        btnBack = findViewById(R.id.btn_back);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnFavorite = findViewById(R.id.btn_favorite);
    }

    private void initializeManagers() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        WishlistManager.initialize(dbHelper, authManager);
        wishlistManager = WishlistManager.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnBuyNow.setOnClickListener(v -> handleBuyNow());
        btnFavorite.setOnClickListener(v -> handleFavorite());
    }

    private void loadProductData() {
        Intent intent = getIntent();
        int productId = intent.getIntExtra("product_id", intent.getIntExtra("productId", 0));

        if (productId == 0) {
            Toast.makeText(this, "Không tìm thấy mã sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestoreRepository.fetchSingleProductById(productId, new FirestoreRepository.ProductCallback() {
            @Override
            public void onSuccess(Product firebaseProduct) {
                product = firebaseProduct;
                bindProduct();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(ProductDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindProduct() {
        product.setFavorite(wishlistManager.isInWishlist(product.getId()));
        tvProductName.setText(safeText(product.getName(), "Sản phẩm"));
        tvProductCategory.setText("Danh mục: " + ProductDisplayUtils.category(product.getCategory()));
        tvProductPrice.setText(String.format(new Locale("vi", "VN"), "%,.0f VND", product.getPrice()));
        tvProductDescription.setText(ProductDisplayUtils.description(product.getDescription()));
        ImageManager.getInstance().loadImageWithAnimation(product.getImageUrl(), imgProductMain);
        updateFavoriteButton();
    }

    private void handleBuyNow() {
        if (product == null) {
            Toast.makeText(this, "Sản phẩm đang tải, vui lòng thử lại", Toast.LENGTH_SHORT).show();
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

    private void updateFavoriteButton() {
        boolean isFavorite = product != null && product.isFavorite();
        btnFavorite.setSelected(isFavorite);
        btnFavorite.setImageResource(isFavorite
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_outline);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
