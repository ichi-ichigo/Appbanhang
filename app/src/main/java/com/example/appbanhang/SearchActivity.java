package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.adapters.ProductAdapter;
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.DataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearch;
    private TextView txtResultCount, txtEmpty;
    private RecyclerView recyclerProducts;
    private ProductAdapter productAdapter;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> visibleProducts = new ArrayList<>();
    private String activeFilter = "ALL";
    private WishlistManager wishlistManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeManagers();
        initializeViews();
        setupProducts();
        setupListeners();
        applyFilter();
    }

    private void initializeManagers() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager authManager = AuthManager.getInstance();
        WishlistManager.initialize(dbHelper, authManager);
        wishlistManager = WishlistManager.getInstance();
    }

    private void initializeViews() {
        etSearch = findViewById(R.id.et_search);
        txtResultCount = findViewById(R.id.txt_result_count);
        txtEmpty = findViewById(R.id.txt_empty);
        recyclerProducts = findViewById(R.id.recycler_search_products);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupProducts() {
        allProducts.clear();
        allProducts.addAll(DataProvider.getProducts());
        new DatabaseHelper(this).seedProductsIfEmpty(allProducts);
        wishlistManager.syncFromDatabase(allProducts);
        for (Product product : allProducts) {
            product.setFavorite(wishlistManager.isInWishlist(product.getId()));
        }

        productAdapter = new ProductAdapter(visibleProducts, this);
        productAdapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(SearchActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Product product, boolean isFavorite) {
                if (isFavorite) {
                    wishlistManager.addToWishlist(product);
                } else {
                    wishlistManager.removeFromWishlistById(product.getId());
                }
                Toast.makeText(SearchActivity.this,
                        isFavorite ? "Da them vao yeu thich" : "Da bo yeu thich",
                        Toast.LENGTH_SHORT).show();
            }
        });

        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setAdapter(productAdapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        bindFilter(R.id.filter_all, "ALL");
        bindFilter(R.id.filter_hot, "HOT DEAL");
        bindFilter(R.id.filter_nike, "NIKE");
        bindFilter(R.id.filter_adidas, "ADIDAS");
        bindFilter(R.id.filter_running, "RUNNING");
        findViewById(R.id.promo_sport).setOnClickListener(v -> {
            activeFilter = "RUNNING";
            applyFilter();
        });
        findViewById(R.id.promo_shoes).setOnClickListener(v -> {
            activeFilter = "HOT DEAL";
            applyFilter();
        });
    }

    private void bindFilter(int buttonId, String filter) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            activeFilter = filter;
            applyFilter();
        });
    }

    private void applyFilter() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        visibleProducts.clear();

        for (Product product : allProducts) {
            if (matchesSearch(product, query) && matchesFilter(product)) {
                visibleProducts.add(product);
            }
        }

        productAdapter.notifyDataSetChanged();
        txtResultCount.setText("San pham phu hop: " + visibleProducts.size());
        txtEmpty.setVisibility(visibleProducts.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(visibleProducts.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private boolean matchesSearch(Product product, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return contains(product.getName(), query)
                || contains(product.getBrand(), query)
                || contains(product.getCategory(), query)
                || contains(product.getPromotion(), query);
    }

    private boolean matchesFilter(Product product) {
        switch (activeFilter) {
            case "HOT DEAL":
                return "HOT DEAL".equalsIgnoreCase(product.getPromotion());
            case "NIKE":
                return "Nike".equalsIgnoreCase(product.getBrand());
            case "ADIDAS":
                return "Adidas".equalsIgnoreCase(product.getBrand());
            case "RUNNING":
                return "Running".equalsIgnoreCase(product.getCategory());
            case "ALL":
            default:
                return true;
        }
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
