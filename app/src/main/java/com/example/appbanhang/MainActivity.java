package com.example.appbanhang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int PRODUCTS_PER_PAGE = 8;
    private static final String NOTIFICATION_PREFS = "order_notification_prefs";
    private static final String READ_ORDER_NOTIFICATION_KEYS = "read_order_notification_keys";

    private ViewPager2 bannerViewPager;
    private View bannerContainer;
    private LinearLayout dotsIndicator;
    private RecyclerView recyclerProducts;
    private Button btnHomePrevPage, btnHomeNextPage;
    private TextView txtHomePageInfo;
    private ImageButton btnHome, btnSearch, btnCart, btnWishlist, btnAccount, btnNotifications;
    private ImageView imgHomeAvatar;
    private TextView txtNotificationBadge;
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
    private ListenerRegistration orderNotificationRegistration;
    private final List<String> orderNotifications = new ArrayList<>();
    private final List<String> orderNotificationKeys = new ArrayList<>();
    private final List<Boolean> orderNotificationReadStates = new ArrayList<>();
    private boolean orderNotificationsInitialized = false;
    private int unreadOrderNotificationCount = 0;

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
        startOrderNotificationListener();
    }

    // Khoi tao cac manager.
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

    // Anh xa view.
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
        btnNotifications = findViewById(R.id.btn_notifications);
        txtNotificationBadge = findViewById(R.id.txt_notification_badge);
        imgHomeAvatar = findViewById(R.id.img_home_avatar);
    }

    // Hien avatar user.
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

    // Tai banner va san pham.
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

    // Cai dat banner.
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

    // Tao cham banner.
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

    // Doi cham banner dang chon.
    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            android.widget.ImageView dot = (android.widget.ImageView) dotsIndicator.getChildAt(i);
            dot.setImageResource(i == position ? android.R.drawable.presence_online : android.R.drawable.presence_offline);
        }
    }

    // Tu dong chuyen banner.
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

    // Dung banner tu dong.
    private void stopBannerAutoScroll() {
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    // Cai dat danh sach san pham.
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

    // Cap nhat trang san pham.
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

    // Tinh tong so trang.
    private int getPageCount(int totalProducts) {
        if (totalProducts <= 0) {
            return 0;
        }
        return (totalProducts + PRODUCTS_PER_PAGE - 1) / PRODUCTS_PER_PAGE;
    }

    // Cap nhat nut phan trang.
    private void updateHomePaginationState(int totalProducts, int totalPages) {
        int displayPage = totalProducts == 0 ? 0 : currentHomePage + 1;
        txtHomePageInfo.setText("Trang " + displayPage + "/" + totalPages);
        setPageButtonState(btnHomePrevPage, currentHomePage > 0);
        setPageButtonState(btnHomeNextPage, totalProducts > 0 && currentHomePage < totalPages - 1);
    }

    // Bat/tat nut trang.
    private void setPageButtonState(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.45f);
    }

    // Gui du lieu san pham.
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

    // Gan su kien nut menu.
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
        btnNotifications.setOnClickListener(v -> showOrderNotifications());
        btnAccount.setOnClickListener(v -> {
            if (authManager.isLoggedIn()) {
                startActivity(new Intent(this, AccountActivity.class));
            } else {
                Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Lang nghe thong bao don hang.
    private void startOrderNotificationListener() {
        if (orderNotificationRegistration != null) {
            orderNotificationRegistration.remove();
            orderNotificationRegistration = null;
        }

        FirebaseUser firebaseUser = com.example.appbanhang.firebase.FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            orderNotifications.clear();
            orderNotificationKeys.clear();
            orderNotificationReadStates.clear();
            unreadOrderNotificationCount = 0;
            updateNotificationBadge();
            return;
        }

        orderNotificationsInitialized = false;
        orderNotificationRegistration = com.example.appbanhang.firebase.FirebaseHelper.getFirestore()
                .collection("orders")
                .whereEqualTo("userUid", firebaseUser.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null) {
                        return;
                    }

                    if (orderNotificationsInitialized) {
                        for (DocumentChange change : snapshot.getDocumentChanges()) {
                            if (change.getType() == DocumentChange.Type.MODIFIED) {
                                String status = safeString(change.getDocument().getString("orderStatus"));
                                String notificationKey = buildOrderNotificationKey(change.getDocument());
                                if (isNotifiableOrderStatus(status) && !isNotificationRead(notificationKey)) {
                                    Toast.makeText(this,
                                            buildOrderNotification(change.getDocument()),
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }

                    orderNotifications.clear();
                    orderNotificationKeys.clear();
                    orderNotificationReadStates.clear();
                    unreadOrderNotificationCount = 0;
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        String status = safeString(document.getString("orderStatus"));
                        if (isNotifiableOrderStatus(status)) {
                            orderNotifications.add(buildOrderNotification(document));
                            String notificationKey = buildOrderNotificationKey(document);
                            boolean read = isNotificationRead(notificationKey);
                            orderNotificationKeys.add(notificationKey);
                            orderNotificationReadStates.add(read);
                            if (!read) {
                                unreadOrderNotificationCount++;
                            }
                        }
                    }
                    orderNotificationsInitialized = true;
                    updateNotificationBadge();
                });
    }

    // Cap nhat so thong bao.
    private void updateNotificationBadge() {
        if (txtNotificationBadge == null) {
            return;
        }
        int count = unreadOrderNotificationCount;
        if (count <= 0) {
            txtNotificationBadge.setVisibility(View.GONE);
            return;
        }
        txtNotificationBadge.setText(count > 9 ? "9+" : String.valueOf(count));
        txtNotificationBadge.setVisibility(View.VISIBLE);
    }

    // Hien danh sach thong bao.
    private void showOrderNotifications() {
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Vui long dang nhap de xem thong bao", Toast.LENGTH_SHORT).show();
            return;
        }
        if (orderNotifications.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Thong bao don hang")
                    .setMessage("Chua co cap nhat moi ve don hang.")
                    .setPositiveButton("Dong", null)
                    .show();
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thong bao don hang")
                .setView(buildNotificationListView())
                .setPositiveButton("Dong", null)
                .create();
        dialog.setOnDismissListener(d -> markCurrentNotificationsAsRead());
        dialog.show();
    }

    // Tao khung thong bao.
    private View buildNotificationListView() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(false);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int outerPadding = dpToPx(14);
        container.setPadding(outerPadding, dpToPx(4), outerPadding, dpToPx(6));

        for (int i = 0; i < orderNotifications.size(); i++) {
            boolean read = i < orderNotificationReadStates.size() && orderNotificationReadStates.get(i);
            container.addView(createNotificationRow(orderNotifications.get(i), read));
        }

        scrollView.addView(container);
        return scrollView;
    }

    // Tao mot dong thong bao.
    private View createNotificationRow(String message, boolean read) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card_soft);
        row.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, dpToPx(6), 0, dpToPx(6));
        row.setLayoutParams(rowParams);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(android.view.Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = new TextView(this);
        title.setText(message);
        title.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        title.setTextSize(14);
        title.setTypeface(Typeface.DEFAULT, read ? Typeface.NORMAL : Typeface.BOLD);
        title.setMaxLines(2);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        header.addView(title, titleParams);

        TextView status = new TextView(this);
        status.setText(read ? "Da doc" : "Chua doc");
        status.setTextSize(11);
        status.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        status.setTextColor(ContextCompat.getColor(this, read ? R.color.text_secondary : R.color.white));
        status.setBackgroundResource(read ? R.drawable.bg_chip : R.drawable.bg_unread_pill);
        status.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        header.addView(status);

        row.addView(header);

        TextView hint = new TextView(this);
        hint.setText(read ? "Ban da xem thong bao nay" : "Thong bao moi tu cap nhat don hang");
        hint.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        hint.setTextSize(12);
        hint.setPadding(0, dpToPx(6), 0, 0);
        row.addView(hint);

        return row;
    }

    // Kiem tra trang thai can bao.
    private boolean isNotifiableOrderStatus(String status) {
        String normalized = status.toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return false;
        }
        return normalized.contains("xac nhan")
                || normalized.contains("xác nhận")
                || normalized.contains("dang giao")
                || normalized.contains("đang giao")
                || normalized.contains("da giao")
                || normalized.contains("đã giao")
                || normalized.contains("huy")
                || normalized.contains("hủy");
    }

    // Tao noi dung thong bao.
    private String buildOrderNotification(DocumentSnapshot document) {
        Object orderId = document.get("orderId");
        String orderLabel = orderId == null ? document.getId() : String.valueOf(orderId);
        String status = safeString(document.getString("orderStatus"));
        if (status.isEmpty()) {
            status = "Da cap nhat";
        }
        return "Don #" + orderLabel + ": " + status;
    }

    // Tao ma thong bao.
    private String buildOrderNotificationKey(DocumentSnapshot document) {
        Object updatedAt = document.get("updatedAt");
        Object orderDate = document.get("orderDate");
        String version = updatedAt == null
                ? String.valueOf(orderDate == null ? "" : orderDate)
                : String.valueOf(updatedAt);
        return document.getId() + "_" + safeString(document.getString("orderStatus")) + "_" + version;
    }

    // Kiem tra da doc.
    private boolean isNotificationRead(String notificationKey) {
        return getReadNotificationKeys().contains(notificationKey);
    }

    // Danh dau da doc.
    private void markCurrentNotificationsAsRead() {
        if (orderNotificationKeys.isEmpty()) {
            return;
        }
        Set<String> readKeys = new HashSet<>(getReadNotificationKeys());
        readKeys.addAll(orderNotificationKeys);
        getSharedPreferences(NOTIFICATION_PREFS, MODE_PRIVATE)
                .edit()
                .putStringSet(READ_ORDER_NOTIFICATION_KEYS, readKeys)
                .apply();
        orderNotificationReadStates.clear();
        for (int i = 0; i < orderNotificationKeys.size(); i++) {
            orderNotificationReadStates.add(true);
        }
        unreadOrderNotificationCount = 0;
        updateNotificationBadge();
    }

    // Lay thong bao da doc.
    private Set<String> getReadNotificationKeys() {
        SharedPreferences preferences = getSharedPreferences(NOTIFICATION_PREFS, MODE_PRIVATE);
        return new HashSet<>(preferences.getStringSet(READ_ORDER_NOTIFICATION_KEYS, new HashSet<>()));
    }

    // Doi dp sang px.
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // Xu ly chuoi null.
    private String safeString(String value) {
        return value == null ? "" : value.trim();
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
        startOrderNotificationListener();
        if (bannerViewPager != null && bannerList != null && !bannerList.isEmpty()) {
            startBannerAutoScroll();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderNotificationRegistration != null) {
            orderNotificationRegistration.remove();
            orderNotificationRegistration = null;
        }
    }
}
