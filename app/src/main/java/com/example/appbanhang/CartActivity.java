package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appbanhang.adapters.CartAdapter;
import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.models.CartItem;
import com.example.appbanhang.models.Voucher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;
    private TextView tvSubtotal;
    private TextView tvShipping;
    private TextView tvTotal;
    private TextView tvVoucherStatus;
    private EditText etPromoCode;
    private ImageButton btnBack;
    private Button btnCheckout;
    private Button btnApplyPromo;
    private Button btnChooseVoucher;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;
    private CartManager cartManager;
    private FirestoreRepository firestoreRepository;
    private final List<Voucher> vouchers = new ArrayList<>();
    private boolean vouchersLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeManagers();
        setupRecyclerView();
        updateTotals();
        setupListeners();
        loadVouchers();
    }

    private void initializeViews() {
        recyclerCart = findViewById(R.id.recycler_cart_items);
        tvSubtotal = findViewById(R.id.txt_subtotal);
        tvShipping = findViewById(R.id.txt_shipping_fee);
        tvTotal = findViewById(R.id.txt_total);
        tvVoucherStatus = findViewById(R.id.txt_voucher_status);
        etPromoCode = findViewById(R.id.et_promo_code);
        btnBack = findViewById(R.id.btn_back);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnApplyPromo = findViewById(R.id.btn_apply_promo);
        btnChooseVoucher = findViewById(R.id.btn_choose_voucher);
    }

    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        cartManager = CartManager.getInstance();
        firestoreRepository = FirestoreRepository.getInstance();
        syncCartItems();
    }

    private void syncCartItems() {
        cartManager.syncCart(new CartManager.CartSyncCallback() {
            @Override
            public void onSuccess() {
                refreshCartUi();
            }

            @Override
            public void onError(String message) {
                refreshCartUi();
            }
        });
    }

    private void refreshCartUi() {
        if (cartAdapter != null) {
            cartAdapter.notifyDataSetChanged();
        }
        updateTotals();
        refreshVoucherStatus();
    }

    private void setupRecyclerView() {
        recyclerCart.setLayoutManager(new LinearLayoutManager(this));

        List<CartItem> cartItems = cartManager.getAllItems();
        cartAdapter = new CartAdapter(cartItems, this);
        cartAdapter.setOnCartItemListener(new CartAdapter.OnCartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                cartManager.updateQuantity(item, newQuantity);
                reapplyCurrentVoucher(false);
                updateTotals();
            }

            @Override
            public void onItemRemoved(CartItem item) {
                cartManager.removeFromCart(item);
                reapplyCurrentVoucher(false);
                updateTotals();
            }
        });
        recyclerCart.setAdapter(cartAdapter);
    }

    private void updateTotals() {
        double subtotal = cartManager.getTotalPrice();
        double shipping = cartManager.isEmpty() ? 0 : cartManager.getShippingFee();
        double total = subtotal + shipping - cartManager.getDiscount();

        tvSubtotal.setText(formatCurrency(subtotal));
        tvShipping.setText(formatCurrency(shipping));
        tvTotal.setText(formatCurrency(total));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnApplyPromo.setOnClickListener(v -> applyPromoCode());
        btnChooseVoucher.setOnClickListener(v -> showVoucherPicker());
        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getAllItems().isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(CartActivity.this, BillingAddressActivity.class));
        });
    }

    private void loadVouchers() {
        vouchersLoading = true;
        btnChooseVoucher.setEnabled(false);
        tvVoucherStatus.setText("Đang tải voucher...");

        firestoreRepository.fetchVouchers(new FirestoreRepository.VouchersCallback() {
            @Override
            public void onSuccess(List<Voucher> loadedVouchers) {
                vouchersLoading = false;
                vouchers.clear();
                vouchers.addAll(loadedVouchers);
                btnChooseVoucher.setEnabled(!getActiveVouchers().isEmpty());
                refreshVoucherStatus();
                reapplyCurrentVoucher(false);
                updateTotals();
            }

            @Override
            public void onError(String errorMessage) {
                vouchersLoading = false;
                vouchers.clear();
                btnChooseVoucher.setEnabled(false);
                tvVoucherStatus.setText("Không tải được voucher");
                Toast.makeText(CartActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVoucherPicker() {
        if (vouchersLoading) {
            Toast.makeText(this, "Đang tải voucher từ Firebase", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Voucher> activeVouchers = getActiveVouchers();
        if (activeVouchers.isEmpty()) {
            Toast.makeText(this, "Chưa có voucher khả dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] items = new CharSequence[activeVouchers.size()];
        for (int i = 0; i < activeVouchers.size(); i++) {
            Voucher voucher = activeVouchers.get(i);
            items[i] = voucher.getCode() + " - " + safeText(voucher.getTitle())
                    + "\n" + buildVoucherSummary(voucher);
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn voucher")
                .setItems(items, (dialog, which) -> {
                    Voucher selectedVoucher = activeVouchers.get(which);
                    etPromoCode.setText(selectedVoucher.getCode());
                    applyVoucher(selectedVoucher, true, true);
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void applyPromoCode() {
        String code = etPromoCode.getText().toString().trim().toUpperCase(Locale.ROOT);
        if (code.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }

        Voucher voucher = findVoucherByCode(code);
        if (voucher == null) {
            removeAppliedVoucher("Mã voucher không tồn tại", true);
            return;
        }

        applyVoucher(voucher, true, true);
    }

    private void reapplyCurrentVoucher(boolean showToastWhenInvalid) {
        String appliedCode = cartManager.getAppliedPromoCode();
        if (appliedCode == null || appliedCode.trim().isEmpty()) {
            refreshVoucherStatus();
            return;
        }
        if (vouchersLoading) {
            refreshVoucherStatus();
            return;
        }

        Voucher voucher = findVoucherByCode(appliedCode);
        if (voucher == null) {
            removeAppliedVoucher("Voucher không còn tồn tại", showToastWhenInvalid);
            return;
        }

        applyVoucher(voucher, false, showToastWhenInvalid);
    }

    private void applyVoucher(Voucher voucher, boolean showSuccessToast, boolean showInvalidToast) {
        String validationError = validateVoucher(voucher);
        if (validationError != null) {
            removeAppliedVoucher(validationError, showInvalidToast);
            return;
        }

        double discount = calculateDiscount(voucher);
        cartManager.applyPromoCode(voucher.getCode(), discount);
        etPromoCode.setText(voucher.getCode());
        tvVoucherStatus.setText("Đang dùng: " + voucher.getCode() + " - " + safeText(voucher.getTitle()));
        updateTotals();

        if (showSuccessToast) {
            Toast.makeText(this, "Đã áp dụng voucher " + voucher.getCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAppliedVoucher(String reason, boolean showToast) {
        cartManager.clearPromoCode();
        etPromoCode.setText("");
        tvVoucherStatus.setText(reason);
        updateTotals();
        if (showToast) {
            Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
        }
    }

    private String validateVoucher(Voucher voucher) {
        if (voucher == null) {
            return "Voucher không hợp lệ";
        }
        if (!voucher.isActive()) {
            return "Voucher đang tắt";
        }
        if (voucher.getUsageLimit() > 0 && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return "Voucher đã hết lượt sử dụng";
        }
        String dateError = validateVoucherDate(voucher);
        if (dateError != null) {
            return dateError;
        }
        if (cartManager.getTotalPrice() < voucher.getMinOrder()) {
            return "Đơn hàng chưa đạt tối thiểu " + formatCurrency(voucher.getMinOrder());
        }
        if (calculateDiscount(voucher) <= 0) {
            return "Voucher không tạo được giảm giá hợp lệ";
        }
        return null;
    }

    private String validateVoucherDate(Voucher voucher) {
        Date today = stripTime(new Date());
        Date startDate = parseVoucherDate(voucher.getStartDate());
        Date endDate = parseVoucherDate(voucher.getEndDate());

        if (startDate != null && today.before(startDate)) {
            return "Voucher chưa đến ngày áp dụng";
        }
        if (endDate != null && today.after(endDate)) {
            return "Voucher đã hết hạn";
        }
        return null;
    }

    private double calculateDiscount(Voucher voucher) {
        double subtotal = cartManager.getTotalPrice();
        double discount;
        if ("fixed".equalsIgnoreCase(voucher.getType())) {
            discount = voucher.getValue();
        } else {
            discount = subtotal * voucher.getValue() / 100.0;
        }

        if (voucher.getMaxDiscount() > 0) {
            discount = Math.min(discount, voucher.getMaxDiscount());
        }
        return Math.min(Math.max(discount, 0), subtotal);
    }

    private Voucher findVoucherByCode(String code) {
        for (Voucher voucher : vouchers) {
            if (voucher.getCode() != null && voucher.getCode().equalsIgnoreCase(code)) {
                return voucher;
            }
        }
        return null;
    }

    private List<Voucher> getActiveVouchers() {
        List<Voucher> activeVouchers = new ArrayList<>();
        for (Voucher voucher : vouchers) {
            if (voucher != null && voucher.isActive()) {
                activeVouchers.add(voucher);
            }
        }
        return activeVouchers;
    }

    private void refreshVoucherStatus() {
        String appliedCode = cartManager.getAppliedPromoCode();
        if (appliedCode != null && !appliedCode.trim().isEmpty()) {
            Voucher appliedVoucher = findVoucherByCode(appliedCode);
            if (appliedVoucher != null) {
                tvVoucherStatus.setText("Đang dùng: " + appliedVoucher.getCode()
                        + " - " + safeText(appliedVoucher.getTitle()));
                return;
            }
        }

        int availableCount = getActiveVouchers().size();
        if (vouchersLoading) {
            tvVoucherStatus.setText("Đang tải voucher...");
        } else if (availableCount == 0) {
            tvVoucherStatus.setText("Chưa có voucher khả dụng từ Firebase");
        } else {
            tvVoucherStatus.setText(availableCount + " voucher từ Firebase");
        }
    }

    private String buildVoucherSummary(Voucher voucher) {
        String valueLabel;
        if ("fixed".equalsIgnoreCase(voucher.getType())) {
            valueLabel = "Giảm " + formatCurrency(voucher.getValue());
        } else {
            valueLabel = "Giảm " + String.format(new Locale("vi", "VN"), "%.0f%%", voucher.getValue());
        }

        if (voucher.getMinOrder() > 0) {
            valueLabel += ", đơn từ " + formatCurrency(voucher.getMinOrder());
        }
        if (voucher.getMaxDiscount() > 0) {
            valueLabel += ", tối đa " + formatCurrency(voucher.getMaxDiscount());
        }
        return valueLabel;
    }

    private String formatCurrency(double amount) {
        return String.format(new Locale("vi", "VN"), "%,.0f VND", amount);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private Date parseVoucherDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            formatter.setLenient(false);
            return stripTime(formatter.parse(value.trim()));
        } catch (ParseException ignored) {
            return null;
        }
    }

    private Date stripTime(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            return formatter.parse(formatter.format(date));
        } catch (ParseException ignored) {
            return date;
        }
    }
}
