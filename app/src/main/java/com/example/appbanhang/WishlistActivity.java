package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.adapters.WishlistAdapter;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Product;

import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView recyclerWishlist;
    private WishlistAdapter wishlistAdapter;
    private Button btnContinueShopping;
    private WishlistManager wishlistManager;

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
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
    }

    private void initializeManagers() {
        wishlistManager = WishlistManager.getInstance();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerWishlist.setLayoutManager(layoutManager);

        List<Product> wishlistItems = wishlistManager.getWishlistItems();
        wishlistAdapter = new WishlistAdapter(wishlistItems, this);
        recyclerWishlist.setAdapter(wishlistAdapter);

        if (wishlistItems.isEmpty()) {
            Toast.makeText(this, "Danh sách yêu thích trống", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupListeners() {
        btnContinueShopping.setOnClickListener(v -> finish());
    }
}
