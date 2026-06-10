package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.DataProvider;

import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView recyclerWishlist;
    private WishlistAdapter wishlistAdapter;
    private Button btnBack, btnContinueShopping;
    private LinearLayout emptyState;
    private CartManager cartManager;
    private WishlistManager wishlistManager;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;

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
    }

    private void initializeViews() {
        recyclerWishlist = findViewById(R.id.recycler_wishlist);
        btnBack = findViewById(R.id.btn_back);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
        emptyState = findViewById(R.id.empty_state);
    }

    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        WishlistManager.initialize(dbHelper, authManager);
        CartManager.initialize(dbHelper, authManager);
        cartManager = CartManager.getInstance();
        wishlistManager = WishlistManager.getInstance();
        wishlistManager.syncFromDatabase(DataProvider.getProducts());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerWishlist.setLayoutManager(layoutManager);

        List<Product> wishlistItems = wishlistManager.getWishlistItems();
        wishlistAdapter = new WishlistAdapter(wishlistItems, this);
        wishlistAdapter.setOnWishlistListener(new WishlistAdapter.OnWishlistListener() {
            @Override
            public void onBuyClick(Product product) {
                cartManager.addToCart(product, 1, "M");
                Intent intent = new Intent(WishlistActivity.this, CartActivity.class);
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

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnContinueShopping.setOnClickListener(v -> finish());
    }

    private void updateEmptyState() {
        boolean isEmpty = wishlistManager.getWishlistItems().isEmpty();
        recyclerWishlist.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
