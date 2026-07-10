package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.adapters.WishlistAdapter;
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Product;

import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView recyclerWishlist;
    private WishlistAdapter wishlistAdapter;
    private ImageButton btnBack;
    private Button btnContinueShopping;
    private LinearLayout emptyState;
    private WishlistManager wishlistManager;
    private FirestoreRepository firestoreRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeManagers();
        setupRecyclerView();
        setupListeners();
        loadWishlistFromFirebase();
    }

    // Anh xa view.
    private void initializeViews() {
        recyclerWishlist = findViewById(R.id.recycler_wishlist);
        btnBack = findViewById(R.id.btn_back);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
        emptyState = findViewById(R.id.empty_state);
    }

    // Khoi tao manager.
    private void initializeManagers() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager authManager = AuthManager.getInstance();
        WishlistManager.initialize(dbHelper, authManager);
        wishlistManager = WishlistManager.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
    }

    // Cai dat danh sach.
    private void setupRecyclerView() {
        recyclerWishlist.setLayoutManager(new LinearLayoutManager(this));

        List<Product> wishlistItems = wishlistManager.getWishlistItems();
        wishlistAdapter = new WishlistAdapter(wishlistItems, this);
        wishlistAdapter.setOnWishlistListener(new WishlistAdapter.OnWishlistListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(WishlistActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onBuyClick(Product product) {
                Intent intent = new Intent(WishlistActivity.this, AddToCartActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onRemoveClick(Product product) {
                wishlistManager.removeFromWishlist(product);
                wishlistAdapter.notifyDataSetChanged();
                updateEmptyState();
                Toast.makeText(WishlistActivity.this, "Đã bỏ khỏi yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerWishlist.setAdapter(wishlistAdapter);
        updateEmptyState();
    }

    // Tai san pham yeu thich.
    private void loadWishlistFromFirebase() {
        firestoreRepository.fetchProducts(new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                wishlistManager.syncFromDatabase(products);
                wishlistAdapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(WishlistActivity.this,
                        "Lỗi tải sản phẩm yêu thích: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                updateEmptyState();
            }
        });
    }

    // Gan su kien nut.
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnContinueShopping.setOnClickListener(v -> finish());
    }

    // Cap nhat trang thai rong.
    private void updateEmptyState() {
        boolean isEmpty = wishlistManager.getWishlistItems().isEmpty();
        recyclerWishlist.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
