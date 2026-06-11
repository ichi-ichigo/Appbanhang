package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Banner;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 bannerViewPager;
    private LinearLayout dotsIndicator;
    private RecyclerView recyclerProducts;
    private Button btnHome, btnSearch, btnCart, btnWishlist, btnAccount;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private CartManager cartManager;
    private AuthManager authManager;
    private WishlistManager wishlistManager;
    private FirestoreRepository firestoreRepository;
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
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeManagers();
        initializeViews();
        fetchData();
        setupNavigationButtons();
    }

    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        cartManager = CartManager.getInstance();
        authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        cartManager.syncFromDatabase();
        wishlistManager = WishlistManager.getInstance();
        WishlistManager.initialize(dbHelper, authManager);
        firestoreRepository = FirestoreRepository.getInstance();
        bannerHandler = new Handler(Looper.getMainLooper());
    }

    private void initializeViews() {
        bannerViewPager = findViewById(R.id.banner_view_pager);
        dotsIndicator = findViewById(R.id.dots_indicator);
        recyclerProducts = findViewById(R.id.recycler_products);
        progressBar = findViewById(R.id.progress_bar);
        btnHome = findViewById(R.id.btn_home);
        btnSearch = findViewById(R.id.btn_search);
        btnCart = findViewById(R.id.btn_cart);
        btnWishlist = findViewById(R.id.btn_wishlist);
        btnAccount = findViewById(R.id.btn_account);
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);

        // Load banners
        firestoreRepository.fetchBanners(new FirestoreRepository.BannersCallback() {
            @Override
            public void onSuccess(List<Banner> banners) {
                bannerList = banners;
                if (!bannerList.isEmpty()) {
                    setupBannerCarousel();
                }
            }
            @Override
            public void onError(String errorMessage) {
                // Banner lỗi không block app
                bannerList = new ArrayList<>();
            }
        });

        // Load products
        firestoreRepository.fetchProducts(new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                progressBar.setVisibility(View.GONE);
                productList = products;
                if (productList.isEmpty()) {
                    Toast.makeText(MainActivity.this,
                        "Chưa có sản phẩm. Hãy thêm trên Firebase!", Toast.LENGTH_LONG).show();
                }
                setupRecyclerView();
            }
            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                productList = new ArrayList<>();
                Toast.makeText(MainActivity.this,
                    "Lỗi tải sản phẩm: " + errorMessage, Toast.LENGTH_LONG).show();
                setupRecyclerView();
            }
        });
    }

    private void setupBannerCarousel() {
        if (bannerList == null || bannerList.isEmpty()) return;
        bannerAdapter = new BannerAdapter(bannerList, this);
        bannerViewPager.setAdapter(bannerAdapter);
        setupDotsIndicator();
        startBannerAutoScroll();
    }

    private void setupDotsIndicator() {
        dotsIndicator.removeAllViews();
        for (int i = 0; i < bannerList.size(); i++) {
            android.widget.ImageView dot = new android.widget.ImageView(this);
            dot.setImageResource(i == 0 ? android.R.drawable.presence_online : android.R.drawable.presence_offline);
            dot.setScaleX(0.6f);
            dot.setScaleY(0.6f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(5, 0, 5, 0);
            dotsIndicator.addView(dot, params);
        }
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
            dot.setImageResource(i == position ? android.R.drawable.presence_online : android.R.drawable.presence_offline);
        }
    }

    private void startBannerAutoScroll() {
        if (bannerList == null || bannerList.isEmpty()) return;
        stopBannerAutoScroll();
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (bannerViewPager.getCurrentItem() + 1) % bannerList.size();
                bannerViewPager.setCurrentItem(nextItem, true);
                bannerHandler.postDelayed(this, 5000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    private void stopBannerAutoScroll() {
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerProducts.setLayoutManager(gridLayoutManager);
        wishlistManager.syncFromDatabase(productList);
        for (Product product : productList) {
            product.setFavorite(wishlistManager.isInWishlist(product.getId()));
        }
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
                if (isFavorite) wishlistManager.addToWishlist(product);
                else wishlistManager.removeFromWishlistById(product.getId());
                Toast.makeText(MainActivity.this, (isFavorite ? "Added to " : "Removed from ") + "Wishlist", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerProducts.setAdapter(adapter);
    }

    private void setupNavigationButtons() {
        btnHome.setOnClickListener(v -> {});
        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnWishlist.setOnClickListener(v -> startActivity(new Intent(this, WishlistActivity.class)));
        btnAccount.setOnClickListener(v -> {
            if (authManager.isLoggedIn()) startActivity(new Intent(this, AccountActivity.class));
            else Toast.makeText(this, "Vui lòng Đăng Nhập", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerViewPager != null && bannerList != null && !bannerList.isEmpty()) {
            startBannerAutoScroll();
        }
    }
}
