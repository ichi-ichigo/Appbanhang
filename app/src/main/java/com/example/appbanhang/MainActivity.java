package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appbanhang.adapters.BannerAdapter;
import com.example.appbanhang.adapters.ProductAdapter;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.models.Banner;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.utils.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 bannerViewPager;
    private LinearLayout dotsIndicator;
    private RecyclerView recyclerProducts;
    private Button btnHome, btnSearch, btnCart, btnWishlist, btnAccount;
    private DatabaseHelper dbHelper;
    private CartManager cartManager;
    private AuthManager authManager;
    private List<Product> productList;
    private List<Banner> bannerList;
    private BannerAdapter bannerAdapter;
    private Handler bannerHandler;
    private Runnable bannerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Áp dụng window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize
        initializeManagers();
        initializeViews();
        loadBanners();
        setupBannerCarousel();
        loadProducts();
        setupRecyclerView();
        setupNavigationButtons();

        // Test login
        testLogin();
    }

    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        cartManager = CartManager.getInstance();
        authManager = AuthManager.getInstance();
        bannerHandler = new Handler(Looper.getMainLooper());
    }

    private void initializeViews() {
        bannerViewPager = findViewById(R.id.banner_view_pager);
        dotsIndicator = findViewById(R.id.dots_indicator);
        recyclerProducts = findViewById(R.id.recycler_products);
        btnHome = findViewById(R.id.btn_home);
        btnSearch = findViewById(R.id.btn_search);
        btnCart = findViewById(R.id.btn_cart);
        btnWishlist = findViewById(R.id.btn_wishlist);
        btnAccount = findViewById(R.id.btn_account);
    }

    private void loadBanners() {
        bannerList = DataProvider.getBanners();
    }

    private void setupBannerCarousel() {
        bannerAdapter = new BannerAdapter(bannerList, this);
        bannerViewPager.setAdapter(bannerAdapter);
        
        // Setup banner click listener
        bannerAdapter.setOnBannerClickListener(banner -> {
            Toast.makeText(MainActivity.this, 
                "Clicked: " + banner.getTitle(), Toast.LENGTH_SHORT).show();
        });

        // Setup dots indicator
        setupDotsIndicator();
        
        // Auto-scroll banner every 5 seconds
        startBannerAutoScroll();
    }

    private void setupDotsIndicator() {
        dotsIndicator.removeAllViews();
        
        for (int i = 0; i < bannerList.size(); i++) {
            android.widget.ImageView dot = new android.widget.ImageView(this);
            dot.setImageResource(i == 0 ? 
                android.R.drawable.presence_online : 
                android.R.drawable.presence_offline);
            dot.setScaleX(0.6f);
            dot.setScaleY(0.6f);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(5, 0, 5, 0);
            
            dotsIndicator.addView(dot, params);
        }
        
        // Update dots when banner changes
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDotsIndicator(position);
            }
        });
    }

    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            android.widget.ImageView dot = (android.widget.ImageView) dotsIndicator.getChildAt(i);
            dot.setImageResource(i == position ? 
                android.R.drawable.presence_online : 
                android.R.drawable.presence_offline);
        }
    }

    private void startBannerAutoScroll() {
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (bannerViewPager.getCurrentItem() + 1) % bannerList.size();
                bannerViewPager.setCurrentItem(nextItem, true);
                bannerHandler.postDelayed(this, 5000); // Auto-scroll every 5 seconds
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    private void stopBannerAutoScroll() {
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void loadProducts() {
        // Load products from DataProvider with images
        productList = DataProvider.getProducts();
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerProducts.setLayoutManager(gridLayoutManager);
        
        ProductAdapter adapter = new ProductAdapter(productList, this);
        adapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Product product, boolean isFavorite) {
                String message = isFavorite ? 
                    "Added to Wishlist: " + product.getName() :
                    "Removed from Wishlist: " + product.getName();
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
        recyclerProducts.setAdapter(adapter);
    }

    private void setupNavigationButtons() {
        btnHome.setOnClickListener(v -> {
            Toast.makeText(this, "Trang Chủ", Toast.LENGTH_SHORT).show();
        });

        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Tìm Kiếm - Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        btnWishlist.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WishlistActivity.class);
            startActivity(intent);
        });

        btnAccount.setOnClickListener(v -> {
            if (authManager.isLoggedIn()) {
                Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Vui lòng Đăng Nhập", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void testLogin() {
        // Test đăng nhập với tài khoản mẫu
        boolean loginSuccess = authManager.login("sultan@example.com", "password123");
        if (loginSuccess) {
            Toast.makeText(this, "Đã đăng nhập: " + 
                authManager.getCurrentUser().getFullName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerViewPager != null && bannerList != null && bannerList.size() > 0) {
            startBannerAutoScroll();
        }
    }
}