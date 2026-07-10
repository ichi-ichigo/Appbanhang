package com.example.appbanhang;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AddToCartActivity extends AppCompatActivity {
    private ImageView imgProduct;
    private LinearLayout thumbnailContainer;
    private TextView txtProductName;
    private TextView txtProductPrice;
    private TextView txtQuantity;
    private Button btnMinus;
    private Button btnPlus;
    private Button btnAddToCart;
    private final List<Button> sizeButtons = new ArrayList<>();
    private Product product;
    private FirestoreRepository firestoreRepository;
    private CartManager cartManager;
    private int quantity = 1;
    private String selectedSize = "41";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_cart);

        setupSafeArea();
        initializeViews();
        initializeManagers();
        loadProduct();
        setupListeners();
    }

    // Cai dat vung an toan.
    private void setupSafeArea() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.add_to_cart_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Khoi tao manager.
    private void initializeManagers() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        cartManager = CartManager.getInstance();
        cartManager.syncCart(new CartManager.CartSyncCallback() {
            @Override
            public void onSuccess() {
                refreshCartState();
            }

            @Override
            public void onError(String message) {
                refreshCartState();
            }
        });
        firestoreRepository = FirestoreRepository.getInstance();
    }

    // Anh xa view.
    private void initializeViews() {
        imgProduct = findViewById(R.id.img_product);
        thumbnailContainer = findViewById(R.id.thumbnail_container);
        txtProductName = findViewById(R.id.txt_product_name);
        txtProductPrice = findViewById(R.id.txt_product_price);
        txtQuantity = findViewById(R.id.txt_quantity);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnMinus.setText("-");
        btnPlus.setText("+");
        btnAddToCart.setText("Them vao gio hang");
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        sizeButtons.add(findViewById(R.id.btn_size_39));
        sizeButtons.add(findViewById(R.id.btn_size_40));
        sizeButtons.add(findViewById(R.id.btn_size_41));
        sizeButtons.add(findViewById(R.id.btn_size_42));
        sizeButtons.add(findViewById(R.id.btn_size_43));
    }

    // Tai san pham.
    private void loadProduct() {
        int productId = getIntent().getIntExtra("product_id", 0);
        if (productId == 0) {
            Toast.makeText(this, "Khong tim thay ma san pham", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AddToCartActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Hien thong tin san pham.
    private void bindProduct() {
        txtProductName.setText(product.getName());
        txtProductPrice.setText(String.format(new Locale("vi", "VN"), "%,.0f VND", product.getPrice()));
        ImageManager.getInstance().loadImageWithAnimation(product.getImageUrl(), imgProduct);
        setupGallery();
        updateSizeButtons();
        refreshCartState();
    }

    // Gan su kien nut.
    private void setupListeners() {
        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
                updateActionState();
            }
        });

        btnPlus.setOnClickListener(v -> {
            int remainingStock = getRemainingStock();
            if (remainingStock <= 0) {
                Toast.makeText(this, "San pham da het hang", Toast.LENGTH_SHORT).show();
                updateActionState();
                return;
            }
            if (quantity >= remainingStock) {
                Toast.makeText(this, "Chi con " + remainingStock + " san pham trong kho", Toast.LENGTH_SHORT).show();
                updateActionState();
                return;
            }

            quantity++;
            updateQuantity();
            updateActionState();
        });

        btnAddToCart.setOnClickListener(v -> {
            if (product == null) {
                return;
            }
            if (getRemainingStock() <= 0) {
                Toast.makeText(this, "San pham da het hang", Toast.LENGTH_SHORT).show();
                updateActionState();
                return;
            }

            cartManager.addToCart(product, quantity, selectedSize);
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

    // Cai dat anh san pham.
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
            thumbnail.setOnClickListener(v ->
                    ImageManager.getInstance().loadImageWithAnimation(imageUrl, imgProduct));
            thumbnailContainer.addView(thumbnail);
        }
    }

    // Cap nhat so luong.
    private void updateQuantity() {
        txtQuantity.setText(String.valueOf(quantity));
    }

    // Tinh hang con lai.
    private int getRemainingStock() {
        if (product == null || cartManager == null) {
            return 0;
        }
        return cartManager.getRemainingStock(product);
    }

    // Cap nhat trang thai gio.
    private void refreshCartState() {
        if (product == null) {
            return;
        }

        int remainingStock = getRemainingStock();
        if (remainingStock > 0 && quantity > remainingStock) {
            quantity = remainingStock;
        }
        if (remainingStock <= 0) {
            quantity = 1;
        }

        updateQuantity();
        updateActionState();
    }

    // Cap nhat nut them gio.
    private void updateActionState() {
        int remainingStock = getRemainingStock();
        boolean canAdd = remainingStock > 0;

        btnAddToCart.setEnabled(canAdd);
        btnAddToCart.setAlpha(canAdd ? 1f : 0.6f);
        btnPlus.setEnabled(canAdd && quantity < remainingStock);
        btnPlus.setAlpha(btnPlus.isEnabled() ? 1f : 0.6f);
        btnMinus.setEnabled(quantity > 1);
        btnMinus.setAlpha(btnMinus.isEnabled() ? 1f : 0.6f);
    }

    // Cap nhat nut size.
    private void updateSizeButtons() {
        for (Button button : sizeButtons) {
            boolean selected = selectedSize.equals(button.getText().toString());
            button.setSelected(selected);
            button.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    // Doi dp sang px.
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
