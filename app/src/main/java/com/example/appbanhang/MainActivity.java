package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Banner;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.models.User;
import com.example.appbanhang.utils.DataProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PRODUCTS_PER_PAGE = 8;

    private ViewPager2 bannerViewPager;
    private View bannerContainer;
    private LinearLayout dotsIndicator;
    private RecyclerView recyclerProducts;
    private Button btnHomePrevPage, btnHomeNextPage;
    private TextView txtHomePageInfo;
    private ImageButton btnHome, btnSearch, btnCart, btnWishlist, btnAccount;
    private ImageView imgHomeAvatar;
    private ProgressBar progressBar;
    private AuthManager authManager;
    private WishlistManager wishlistManager;
    private FirestoreRepository firestoreRepository;
    private List<Product> productList = new ArrayList<>();
    private final List<Product> pagedProductList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private int currentHomePage = 0;
    private List<Banner> bannerList;
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
        bindCurrentUserAvatar();
        fetchData();
        setupNavigationButtons();
    }

    private void initializeManagers() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        CartManager.getInstance().syncCart();
        WishlistManager.initialize(dbHelper, authManager);
        wishlistManager = WishlistManager.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
        bannerHandler = new Handler(Looper.getMainLooper());
    }

    private void initializeViews() {
        bannerViewPager = findViewById(R.id.banner_view_pager);
        bannerContainer = findViewById(R.id.banner_container);
        dotsIndicator = findViewById(R.id.dots_indicator);
        recyclerProducts = findViewById(R.id.recycler_products);
        progressBar = findViewById(R.id.progress_bar);
        btnHomePrevPage = findViewById(R.id.btn_home_prev_page);
        btnHomeNextPage = findViewById(R.id.btn_home_next_page);
        txtHomePageInfo = findViewById(R.id.txt_home_page_info);
        txtHomePageInfo.setText("Trang 0/0");
        setPageButtonState(btnHomePrevPage, false);
        setPageButtonState(btnHomeNextPage, false);
        btnHome = findViewById(R.id.btn_home);
        btnSearch = findViewById(R.id.btn_search);
        btnCart = findViewById(R.id.btn_cart);
        btnWishlist = findViewById(R.id.btn_wishlist);
        btnAccount = findViewById(R.id.btn_account);
        imgHomeAvatar = findViewById(R.id.img_home_avatar);
    }

    private void bindCurrentUserAvatar() {
        if (!authManager.isLoggedIn()) {
            if (imgHomeAvatar != null) {
                imgHomeAvatar.setImageResource(R.drawable.ic_user_avatar);
            }
            return;
        }

        authManager.ensureCurrentUser(new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                ImageManager.getInstance().loadAvatar(user.getAvatar(), imgHomeAvatar);
            }

            @Override
            public void onError(String message) {
                if (imgHomeAvatar != null) {
                    imgHomeAvatar.setImageResource(R.drawable.ic_user_avatar);
                }
            }
        });
    }

    private void fetchData() {
        progressBar.setVisibility(View.VISIBLE);

        firestoreRepository.fetchBanners(new FirestoreRepository.BannersCallback() {
            @Override
            public void onSuccess(List<Banner> banners) {
                bannerList = banners.isEmpty() ? DataProvider.getBanners() : banners;
                setupBannerCarousel();
            }

            @Override
            public void onError(String errorMessage) {
                bannerList = DataProvider.getBanners();
                setupBannerCarousel();
            }
        });

        firestoreRepository.fetchProducts(new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                progressBar.setVisibility(View.GONE);
                productList = products == null ? java.util.Collections.emptyList() : products;
                currentHomePage = 0;
                setupRecyclerView();
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                productList = java.util.Collections.emptyList();
                currentHomePage = 0;
                Toast.makeText(MainActivity.this,
                        "Lỗi tải sản phẩm: " + errorMessage,
                        Toast.LENGTH_LONG).show();
                setupRecyclerView();
            }
        });
    }

    private void setupBannerCarousel() {
        if (bannerList == null || bannerList.isEmpty()) {
            bannerContainer.setVisibility(View.GONE);
            return;
        }
        bannerContainer.setVisibility(View.VISIBLE);
        bannerViewPager.setAdapter(new BannerAdapter(bannerList, this));
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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
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
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setNestedScrollingEnabled(false);
        recyclerProducts.setHasFixedSize(false);
        wishlistManager.syncFromDatabase(productList);
        for (Product product : productList) {
            product.setFavorite(wishlistManager.isInWishlist(product.getId()));
        }

        if (productAdapter == null) {
            productAdapter = new ProductAdapter(pagedProductList, this);
            productAdapter.setOnProductClickListener(new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                putProductExtras(intent, product);
                startActivity(intent);
            }

            @Override
            public void onFavoriteClick(Product product, boolean isFavorite) {
                if (isFavorite) wishlistManager.addToWishlist(product);
                else wishlistManager.removeFromWishlistById(product.getId());
                Toast.makeText(MainActivity.this,
                        isFavorite ? "Đã thêm vào yêu thích" : "Đã bỏ khỏi yêu thích",
                        Toast.LENGTH_SHORT).show();
            }
        });
            recyclerProducts.setAdapter(productAdapter);
        }

        updateHomePage();
    }

    private void updateHomePage() {
        int totalProducts = productList == null ? 0 : productList.size();
        int totalPages = getPageCount(totalProducts);

        if (totalPages == 0) {
            currentHomePage = 0;
        } else if (currentHomePage >= totalPages) {
            currentHomePage = totalPages - 1;
        }

        int start = currentHomePage * PRODUCTS_PER_PAGE;
        int end = Math.min(start + PRODUCTS_PER_PAGE, totalProducts);

        pagedProductList.clear();
        if (start < end) {
            pagedProductList.addAll(productList.subList(start, end));
        }

        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        updateHomePaginationState(totalProducts, totalPages);
    }

    private int getPageCount(int totalProducts) {
        if (totalProducts <= 0) {
            return 0;
        }
        return (totalProducts + PRODUCTS_PER_PAGE - 1) / PRODUCTS_PER_PAGE;
    }

    private void updateHomePaginationState(int totalProducts, int totalPages) {
        int displayPage = totalProducts == 0 ? 0 : currentHomePage + 1;
        txtHomePageInfo.setText("Trang " + displayPage + "/" + totalPages);
        setPageButtonState(btnHomePrevPage, currentHomePage > 0);
        setPageButtonState(btnHomeNextPage, totalProducts > 0 && currentHomePage < totalPages - 1);
    }

    private void setPageButtonState(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.45f);
    }

    private void putProductExtras(Intent intent, Product product) {
        intent.putExtra("product_id", product.getId());
        intent.putExtra("productId", product.getId());
        intent.putExtra("productName", product.getName());
        intent.putExtra("productCategory", product.getCategory());
        intent.putExtra("productPrice", product.getPrice());
        intent.putExtra("productImage", product.getImageUrl());
        intent.putExtra("productDescription", product.getDescription());
        intent.putExtra("productRating", product.getRating());
        intent.putExtra("productBrand", product.getBrand());
    }

    private void setupNavigationButtons() {
        btnHome.setOnClickListener(v -> {});
        btnHomePrevPage.setOnClickListener(v -> {
            if (currentHomePage > 0) {
                currentHomePage--;
                updateHomePage();
                recyclerProducts.post(() -> recyclerProducts.scrollToPosition(0));
            }
        });
        btnHomeNextPage.setOnClickListener(v -> {
            int totalPages = getPageCount(productList == null ? 0 : productList.size());
            if (currentHomePage < totalPages - 1) {
                currentHomePage++;
                updateHomePage();
                recyclerProducts.post(() -> recyclerProducts.scrollToPosition(0));
            }
        });
        btnSearch.setOnClickListener(v -> startActivity(new Intent(this, SearchActivity.class)));
        btnCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnWishlist.setOnClickListener(v -> startActivity(new Intent(this, WishlistActivity.class)));
        btnAccount.setOnClickListener(v -> {
            if (authManager.isLoggedIn()) {
                startActivity(new Intent(this, AccountActivity.class));
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
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
        bindCurrentUserAvatar();
        if (bannerViewPager != null && bannerList != null && !bannerList.isEmpty()) {
            startBannerAutoScroll();
        }
    }
}
