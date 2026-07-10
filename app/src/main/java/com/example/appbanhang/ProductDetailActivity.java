package com.example.appbanhang;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirebaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.managers.ImageManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.ProductDisplayUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imgProductMain;
    private TextView tvProductName, tvProductCategory, tvProductPrice, tvProductDescription, tvProductRating;
    private TextView tvReviewStatus, tvReviewEmpty;
    private EditText edtReviewComment;
    private LinearLayout ratingStarContainer, reviewListContainer;
    private ImageButton btnBack, btnFavorite;
    private Button btnBuyNow, btnRateProduct, btnSubmitReview;
    private WishlistManager wishlistManager;
    private FirestoreRepository firestoreRepository;
    private Product product;
    private final TextView[] ratingStars = new TextView[5];
    private int selectedRating = 5;
    private boolean userAlreadyReviewed = false;

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

    // Anh xa view.
    private void initializeViews() {
        imgProductMain = findViewById(R.id.img_product_main);
        tvProductName = findViewById(R.id.txt_product_name);
        tvProductCategory = findViewById(R.id.txt_product_category);
        tvProductPrice = findViewById(R.id.txt_product_price);
        tvProductDescription = findViewById(R.id.txt_product_description);
        tvProductRating = findViewById(R.id.txt_product_rating);
        tvReviewStatus = findViewById(R.id.txt_review_status);
        tvReviewEmpty = findViewById(R.id.txt_review_empty);
        edtReviewComment = findViewById(R.id.edt_review_comment);
        ratingStarContainer = findViewById(R.id.rating_star_container);
        reviewListContainer = findViewById(R.id.review_list_container);
        btnBack = findViewById(R.id.btn_back);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        btnRateProduct = findViewById(R.id.btn_rate_product);
        btnSubmitReview = findViewById(R.id.btn_submit_review);
        btnFavorite = findViewById(R.id.btn_favorite);
        setupRatingStars();
    }

    // Khoi tao manager.
    private void initializeManagers() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        WishlistManager.initialize(dbHelper, authManager);
        wishlistManager = WishlistManager.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
    }

    // Gan su kien nut.
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnBuyNow.setOnClickListener(v -> handleBuyNow());
        btnFavorite.setOnClickListener(v -> handleFavorite());
        btnRateProduct.setOnClickListener(v -> scrollToReviewForm());
        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    // Tai du lieu san pham.
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

    // Hien thong tin san pham.
    private void bindProduct() {
        product.setFavorite(wishlistManager.isInWishlist(product.getId()));
        tvProductName.setText(safeText(product.getName(), "Sản phẩm"));
        tvProductCategory.setText("Danh mục: " + ProductDisplayUtils.category(product.getCategory()));
        tvProductPrice.setText(String.format(new Locale("vi", "VN"), "%,.0f VND", product.getPrice()));
        tvProductDescription.setText(ProductDisplayUtils.description(product.getDescription()));
        updateRatingText();
        ImageManager.getInstance().loadImageWithAnimation(product.getImageUrl(), imgProductMain);
        updateFavoriteButton();
        checkCurrentUserReview();
        loadProductReviews();
    }

    // Cuon den form danh gia.
    private void scrollToReviewForm() {
        edtReviewComment.requestFocus();
    }

    // Gui danh gia.
    private void submitReview() {
        if (product == null) {
            Toast.makeText(this, "San pham dang tai, vui long thu lai", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Vui long dang nhap de danh gia", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userAlreadyReviewed) {
            Toast.makeText(this, "Ban da danh gia san pham nay roi", Toast.LENGTH_SHORT).show();
            return;
        }

        String comment = edtReviewComment.getText().toString().trim();
        if (comment.isEmpty()) {
            edtReviewComment.setError("Vui long viet binh luan");
            return;
        }

        btnSubmitReview.setEnabled(false);
        String reviewId = buildReviewId(user.getUid());
        FirebaseHelper.getFirestore()
                .collection("product_reviews")
                .document(reviewId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        userAlreadyReviewed = true;
                        updateReviewFormState();
                        Toast.makeText(this, "Ban da danh gia san pham nay roi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveReview(user, comment, reviewId);
                })
                .addOnFailureListener(error -> {
                    btnSubmitReview.setEnabled(true);
                    Toast.makeText(this, "Loi kiem tra danh gia: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Luu danh gia.
    private void saveReview(FirebaseUser user, String comment, String reviewId) {
        Map<String, Object> review = new HashMap<>();
        review.put("productId", product.getId());
        review.put("productName", product.getName());
        review.put("userUid", user.getUid());
        review.put("userEmail", user.getEmail() == null ? "" : user.getEmail());
        review.put("rating", selectedRating);
        review.put("comment", comment);
        review.put("createdAt", FieldValue.serverTimestamp());

        FirebaseHelper.getFirestore()
                .collection("product_reviews")
                .document(reviewId)
                .set(review)
                .addOnSuccessListener(unused -> updateProductRatingAfterReview())
                .addOnFailureListener(error -> {
                    btnSubmitReview.setEnabled(true);
                    Toast.makeText(this, "Loi gui danh gia: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Cap nhat sao san pham.
    private void updateProductRatingAfterReview() {
        int currentCount = Math.max(product.getReviewCount(), 0);
        double currentRating = product.getRating() <= 0 ? 0 : product.getRating();
        double newRating = ((currentRating * currentCount) + selectedRating) / (currentCount + 1);

        product.setRating(newRating);
        product.setReviewCount(currentCount + 1);
        firestoreRepository.updateProduct(product, new FirestoreRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                userAlreadyReviewed = true;
                edtReviewComment.setText("");
                updateRatingText();
                updateReviewFormState();
                loadProductReviews();
                Toast.makeText(ProductDetailActivity.this, "Da gui danh gia san pham", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errorMessage) {
                userAlreadyReviewed = true;
                updateRatingText();
                updateReviewFormState();
                loadProductReviews();
                Toast.makeText(ProductDetailActivity.this, "Da luu binh luan, chua cap nhat duoc diem sao", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Kiem tra user da danh gia.
    private void checkCurrentUserReview() {
        FirebaseUser user = FirebaseHelper.getAuth().getCurrentUser();
        if (user == null || product == null) {
            userAlreadyReviewed = false;
            updateReviewFormState();
            return;
        }

        FirebaseHelper.getFirestore()
                .collection("product_reviews")
                .document(buildReviewId(user.getUid()))
                .get()
                .addOnSuccessListener(document -> {
                    userAlreadyReviewed = document.exists();
                    updateReviewFormState();
                })
                .addOnFailureListener(error -> updateReviewFormState());
    }

    // Tai danh sach danh gia.
    private void loadProductReviews() {
        if (product == null) {
            return;
        }
        FirebaseHelper.getFirestore()
                .collection("product_reviews")
                .whereEqualTo("productId", product.getId())
                .get()
                .addOnSuccessListener(snapshot -> renderReviews(snapshot.getDocuments()))
                .addOnFailureListener(error -> {
                    reviewListContainer.removeAllViews();
                    tvReviewEmpty.setText("Khong tai duoc danh gia.");
                    tvReviewEmpty.setVisibility(View.VISIBLE);
                });
    }

    // Hien danh sach danh gia.
    private void renderReviews(List<DocumentSnapshot> reviews) {
        reviewListContainer.removeAllViews();
        tvReviewEmpty.setVisibility(reviews.isEmpty() ? View.VISIBLE : View.GONE);
        for (DocumentSnapshot review : reviews) {
            reviewListContainer.addView(createReviewRow(review));
        }
    }

    // Tao mot dong danh gia.
    private View createReviewRow(DocumentSnapshot review) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card_soft);
        row.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(10));
        row.setLayoutParams(params);

        TextView header = new TextView(this);
        header.setText(buildReviewStars(getReviewRating(review)) + "  " + safeText(review.getString("userEmail"), "Nguoi dung"));
        header.setTextColor(Color.parseColor("#92400E"));
        header.setTextSize(14);
        header.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        row.addView(header);

        TextView comment = new TextView(this);
        comment.setText(safeText(review.getString("comment"), "Khong co binh luan"));
        comment.setTextColor(Color.parseColor("#4B5563"));
        comment.setTextSize(13);
        comment.setPadding(0, dpToPx(6), 0, 0);
        row.addView(comment);

        return row;
    }

    // Cai dat nut sao.
    private void setupRatingStars() {
        ratingStarContainer.removeAllViews();
        for (int i = 0; i < ratingStars.length; i++) {
            final int rating = i + 1;
            TextView star = new TextView(this);
            star.setTextSize(32);
            star.setTypeface(Typeface.DEFAULT_BOLD);
            star.setPadding(0, 0, dpToPx(8), 0);
            star.setOnClickListener(v -> {
                selectedRating = rating;
                updateStarInput();
            });
            ratingStars[i] = star;
            ratingStarContainer.addView(star);
        }
        updateStarInput();
    }

    // Cap nhat sao dang chon.
    private void updateStarInput() {
        for (int i = 0; i < ratingStars.length; i++) {
            boolean selected = i < selectedRating;
            ratingStars[i].setText(selected ? "★" : "☆");
            ratingStars[i].setTextColor(selected ? Color.parseColor("#FACC15") : Color.parseColor("#9CA3AF"));
        }
    }

    // Cap nhat form danh gia.
    private void updateReviewFormState() {
        boolean enabled = !userAlreadyReviewed && FirebaseHelper.getAuth().getCurrentUser() != null;
        btnSubmitReview.setEnabled(enabled);
        edtReviewComment.setEnabled(enabled);
        for (TextView star : ratingStars) {
            if (star != null) {
                star.setEnabled(enabled);
            }
        }
        if (FirebaseHelper.getAuth().getCurrentUser() == null) {
            tvReviewStatus.setText("Dang nhap de gui danh gia.");
            tvReviewStatus.setVisibility(View.VISIBLE);
        } else if (userAlreadyReviewed) {
            tvReviewStatus.setText("Ban da danh gia san pham nay. Moi user chi duoc danh gia 1 lan.");
            tvReviewStatus.setVisibility(View.VISIBLE);
        } else {
            tvReviewStatus.setVisibility(View.GONE);
        }
    }

    // Tao id danh gia.
    private String buildReviewId(String userUid) {
        return product.getId() + "_" + userUid;
    }

    // Lay so sao danh gia.
    private int getReviewRating(DocumentSnapshot review) {
        Object value = review.get("rating");
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    // Tao chuoi hien thi sao.
    private String buildReviewStars(int rating) {
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            builder.append(i <= rating ? "★" : "☆");
        }
        return builder.toString();
    }

    // Doi dp sang px.
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    // Cap nhat text danh gia.
    private void updateRatingText() {
        if (product == null || tvProductRating == null) {
            return;
        }
        tvProductRating.setText(String.format(
                new Locale("vi", "VN"),
                "Rating: %.1f/5 (%d danh gia)",
                product.getRating(),
                Math.max(product.getReviewCount(), 0)
        ));
    }

    // Xu ly mua ngay.
    private void handleBuyNow() {
        if (product == null) {
            Toast.makeText(this, "Sản phẩm đang tải, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(ProductDetailActivity.this, AddToCartActivity.class);
        intent.putExtra("product_id", product.getId());
        startActivity(intent);
    }

    // Xu ly yeu thich.
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

    // Cap nhat nut yeu thich.
    private void updateFavoriteButton() {
        boolean isFavorite = product != null && product.isFavorite();
        btnFavorite.setSelected(isFavorite);
        btnFavorite.setImageResource(isFavorite
                ? R.drawable.ic_favorite_filled
                : R.drawable.ic_favorite_outline);
    }

    // Xu ly chuoi rong.
    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
