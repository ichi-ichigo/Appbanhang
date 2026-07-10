package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.adapters.ProductAdapter;
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.WishlistManager;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.utils.ProductDisplayUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchActivity extends AppCompatActivity {
    private static final int PRODUCTS_PER_PAGE = 8;

    private EditText etSearch;
    private TextView txtResultCount;
    private TextView txtEmpty;
    private TextView txtSearchPageInfo;
    private Spinner spPriceSort;
    private RecyclerView recyclerProducts;
    private View searchPagination;
    private LinearLayout brandFilterContainer;
    private Button btnSearchPrevPage, btnSearchNextPage;
    private ProductAdapter productAdapter;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> filteredProducts = new ArrayList<>();
    private final List<Product> visibleProducts = new ArrayList<>();
    private String activeFilter = "ALL";
    private String priceSortMode = "DEFAULT";
    private int currentSearchPage = 0;
    private WishlistManager wishlistManager;
    private FirestoreRepository firestoreRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeManagers();
        initializeViews();
        setupProducts();
        setupListeners();
    }

    // Khoi tao manager.
    private void initializeManagers() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        AuthManager authManager = AuthManager.getInstance();
        WishlistManager.initialize(dbHelper, authManager);
        wishlistManager = WishlistManager.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
    }

    // Anh xa view.
    private void initializeViews() {
        etSearch = findViewById(R.id.et_search);
        txtResultCount = findViewById(R.id.txt_result_count);
        txtEmpty = findViewById(R.id.txt_empty);
        txtSearchPageInfo = findViewById(R.id.txt_search_page_info);
        spPriceSort = findViewById(R.id.spinner_price_sort);
        recyclerProducts = findViewById(R.id.recycler_search_products);
        searchPagination = findViewById(R.id.search_pagination);
        brandFilterContainer = findViewById(R.id.brand_filter_container);
        btnSearchPrevPage = findViewById(R.id.btn_search_prev_page);
        btnSearchNextPage = findViewById(R.id.btn_search_next_page);
        searchPagination.setVisibility(View.GONE);
        txtSearchPageInfo.setText("Trang 0/0");
        setPageButtonState(btnSearchPrevPage, false);
        setPageButtonState(btnSearchNextPage, false);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    // Tai san pham.
    private void setupProducts() {
        allProducts.clear();
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
                Toast.makeText(
                        SearchActivity.this,
                        isFavorite ? "Da them vao yeu thich" : "Da bo yeu thich",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setNestedScrollingEnabled(true);
        recyclerProducts.setHasFixedSize(false);
        recyclerProducts.setAdapter(productAdapter);
        txtResultCount.setText("Dang tai san pham...");

        firestoreRepository.fetchProducts(new FirestoreRepository.ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                allProducts.clear();
                if (products != null) {
                    allProducts.addAll(products);
                }
                wishlistManager.syncFromDatabase(allProducts);
                for (Product product : allProducts) {
                    product.setFavorite(wishlistManager.isInWishlist(product.getId()));
                }
                setupBrandFilters();
                currentSearchPage = 0;
                applyFilter();
            }

            @Override
            public void onError(String errorMessage) {
                allProducts.clear();
                filteredProducts.clear();
                setupBrandFilters();
                currentSearchPage = 0;
                applyFilter();
                Toast.makeText(
                        SearchActivity.this,
                        "Loi tai san pham tu Firebase: " + errorMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    // Gan su kien nut.
    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchPage = 0;
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setupBrandFilters();
        setupPriceSort();
        setupPaginationButtons();
    }

    // Tao loc thuong hieu.
    private void setupBrandFilters() {
        if (brandFilterContainer == null) {
            return;
        }

        Set<String> brands = new LinkedHashSet<>();
        for (Product product : allProducts) {
            String brand = normalizeBrand(product.getBrand());
            if (!brand.isEmpty()) {
                brands.add(brand);
            }
        }
        if (!"ALL".equals(activeFilter) && !containsBrand(brands, activeFilter)) {
            activeFilter = "ALL";
        }

        brandFilterContainer.removeAllViews();
        addBrandFilterButton("Tat ca", "ALL");
        for (String brand : brands) {
            addBrandFilterButton(brand, brand);
        }
        updateBrandFilterButtons();
    }

    // Them nut loc hang.
    private void addBrandFilterButton(String label, String filter) {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(40)
        );
        params.setMargins(0, 0, dpToPx(8), 0);
        button.setLayoutParams(params);
        button.setMinWidth(0);
        button.setMinHeight(0);
        button.setPadding(dpToPx(14), 0, dpToPx(14), 0);
        button.setText(label);
        button.setTag(filter);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setOnClickListener(v -> {
            activeFilter = filter;
            currentSearchPage = 0;
            updateBrandFilterButtons();
            applyFilter();
        });
        brandFilterContainer.addView(button);
    }

    // Cap nhat nut loc hang.
    private void updateBrandFilterButtons() {
        if (brandFilterContainer == null) {
            return;
        }

        for (int i = 0; i < brandFilterContainer.getChildCount(); i++) {
            View child = brandFilterContainer.getChildAt(i);
            if (!(child instanceof Button)) {
                continue;
            }

            Button button = (Button) child;
            Object filter = button.getTag();
            boolean selected = filter instanceof String && isSameFilter(activeFilter, (String) filter);

            button.setBackgroundResource(selected ? R.drawable.button_brand_style : R.drawable.bg_chip);
            button.setTextColor(ContextCompat.getColor(
                    this,
                    selected ? android.R.color.white : R.color.text_primary
            ));
        }
    }

    // Ap dung bo loc.
    private void applyFilter() {
        String query = etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        filteredProducts.clear();

        for (Product product : allProducts) {
            if (matchesSearch(product, query) && matchesFilter(product)) {
                filteredProducts.add(product);
            }
        }

        sortFilteredProducts();
        updateSearchPage();

        boolean empty = filteredProducts.isEmpty();
        txtResultCount.setText("San pham phu hop: " + filteredProducts.size());
        txtEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recyclerProducts.setVisibility(empty ? View.GONE : View.VISIBLE);
        searchPagination.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    // Cai dat nut phan trang.
    private void setupPaginationButtons() {
        btnSearchPrevPage.setOnClickListener(v -> {
            if (currentSearchPage > 0) {
                currentSearchPage--;
                updateSearchPage();
                recyclerProducts.scrollToPosition(0);
            }
        });
        btnSearchNextPage.setOnClickListener(v -> {
            int totalPages = getPageCount(filteredProducts.size());
            if (currentSearchPage < totalPages - 1) {
                currentSearchPage++;
                updateSearchPage();
                recyclerProducts.scrollToPosition(0);
            }
        });
    }

    // Cap nhat trang tim kiem.
    private void updateSearchPage() {
        int totalProducts = filteredProducts.size();
        int totalPages = getPageCount(totalProducts);

        if (totalPages == 0) {
            currentSearchPage = 0;
        } else if (currentSearchPage >= totalPages) {
            currentSearchPage = totalPages - 1;
        }

        int start = currentSearchPage * PRODUCTS_PER_PAGE;
        int end = Math.min(start + PRODUCTS_PER_PAGE, totalProducts);

        visibleProducts.clear();
        if (start < end) {
            visibleProducts.addAll(filteredProducts.subList(start, end));
        }

        productAdapter.notifyDataSetChanged();
        updateSearchPaginationState(totalProducts, totalPages);
    }

    // Tinh tong so trang.
    private int getPageCount(int totalProducts) {
        if (totalProducts <= 0) {
            return 0;
        }
        return (totalProducts + PRODUCTS_PER_PAGE - 1) / PRODUCTS_PER_PAGE;
    }

    // Cap nhat phan trang.
    private void updateSearchPaginationState(int totalProducts, int totalPages) {
        int displayPage = totalProducts == 0 ? 0 : currentSearchPage + 1;
        txtSearchPageInfo.setText("Trang " + displayPage + "/" + totalPages);
        setPageButtonState(btnSearchPrevPage, currentSearchPage > 0);
        setPageButtonState(btnSearchNextPage, totalProducts > 0 && currentSearchPage < totalPages - 1);
    }

    // Bat/tat nut trang.
    private void setPageButtonState(Button button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.45f);
    }

    // Cai dat sap xep gia.
    private void setupPriceSort() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Mac dinh", "Gia thap-cao", "Gia cao-thap"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPriceSort.setAdapter(adapter);
        spPriceSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String nextSortMode;
                if (position == 1) {
                    nextSortMode = "LOW_HIGH";
                } else if (position == 2) {
                    nextSortMode = "HIGH_LOW";
                } else {
                    nextSortMode = "DEFAULT";
                }

                if (!nextSortMode.equals(priceSortMode)) {
                    priceSortMode = nextSortMode;
                    currentSearchPage = 0;
                    applyFilter();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    // Sap xep san pham loc.
    private void sortFilteredProducts() {
        if ("LOW_HIGH".equals(priceSortMode)) {
            filteredProducts.sort((first, second) ->
                    Double.compare(first.getPrice(), second.getPrice()));
        } else if ("HIGH_LOW".equals(priceSortMode)) {
            filteredProducts.sort((first, second) ->
                    Double.compare(second.getPrice(), first.getPrice()));
        }
    }

    // Kiem tra khop tu khoa.
    private boolean matchesSearch(Product product, String query) {
        if (query.isEmpty()) {
            return true;
        }
        return contains(product.getName(), query)
                || contains(product.getBrand(), query)
                || contains(product.getCategory(), query)
                || contains(ProductDisplayUtils.category(product.getCategory()), query)
                || contains(product.getPromotion(), query)
                || contains(ProductDisplayUtils.promotion(product.getPromotion()), query);
    }

    // Kiem tra khop bo loc.
    private boolean matchesFilter(Product product) {
        return "ALL".equals(activeFilter) || isSameFilter(activeFilter, product.getBrand());
    }

    // Kiem tra co chua chuoi.
    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    // Chuan hoa ten hang.
    private String normalizeBrand(String brand) {
        return brand == null ? "" : brand.trim();
    }

    // So sanh bo loc.
    private boolean isSameFilter(String first, String second) {
        return normalizeBrand(first).equalsIgnoreCase(normalizeBrand(second));
    }

    // Kiem tra hang da co.
    private boolean containsBrand(Set<String> brands, String brand) {
        for (String item : brands) {
            if (isSameFilter(item, brand)) {
                return true;
            }
        }
        return false;
    }

    // Doi dp sang px.
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
