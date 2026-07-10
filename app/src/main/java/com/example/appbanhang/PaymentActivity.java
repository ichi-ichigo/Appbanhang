package com.example.appbanhang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirebaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.managers.CartManager;
import com.example.appbanhang.models.CartItem;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.models.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private RadioButton rbOnlinePayment;
    private RadioButton rbCashOnDelivery;
    private LinearLayout paymentOnline;
    private LinearLayout paymentCashOnDelivery;
    private LinearLayout paymentQrContainer;
    private TextView txtQrInfo;
    private TextView tvSubtotal;
    private TextView tvShipping;
    private TextView tvTotal;
    private Button btnPay;
    private ImageButton btnBack;
    private CartManager cartManager;
    private AuthManager authManager;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeManagers();
        displayTotal();
        setupListeners();
    }

    // Anh xa view.
    private void initializeViews() {
        rbOnlinePayment = findViewById(R.id.radio_online_payment);
        rbCashOnDelivery = findViewById(R.id.radio_cash_on_delivery);
        paymentOnline = findViewById(R.id.payment_online);
        paymentCashOnDelivery = findViewById(R.id.payment_cash_on_delivery);
        paymentQrContainer = findViewById(R.id.payment_qr_container);
        txtQrInfo = findViewById(R.id.txt_qr_info);
        tvSubtotal = findViewById(R.id.txt_subtotal);
        tvShipping = findViewById(R.id.txt_shipping);
        tvTotal = findViewById(R.id.txt_total_payment);
        btnPay = findViewById(R.id.btn_place_order);
        btnBack = findViewById(R.id.btn_back);
        selectPaymentMethod(false);
    }

    // Khoi tao manager.
    private void initializeManagers() {
        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        CartManager.initialize(dbHelper, authManager);
        cartManager = CartManager.getInstance();
        cartManager.syncCart(new CartManager.CartSyncCallback() {
            @Override
            public void onSuccess() {
                displayTotal();
            }

            @Override
            public void onError(String message) {
                displayTotal();
            }
        });
    }

    // Hien tong tien.
    private void displayTotal() {
        double subtotal = cartManager.getTotalPrice();
        double shipping = cartManager.isEmpty() ? 0 : cartManager.getShippingFee();
        double total = subtotal + shipping - cartManager.getDiscount();
        tvSubtotal.setText(String.format(new Locale("vi", "VN"), "%,.0f VND", subtotal));
        tvShipping.setText(String.format(new Locale("vi", "VN"), "%,.0f VND", shipping));
        tvTotal.setText(String.format(new Locale("vi", "VN"), "%,.0f VND", total));
        updateQrInfo(total);
    }

    // Gan su kien nut.
    private void setupListeners() {
        btnPay.setOnClickListener(v -> handlePayment());
        btnBack.setOnClickListener(v -> finish());
        rbOnlinePayment.setOnClickListener(v -> selectPaymentMethod(true));
        rbCashOnDelivery.setOnClickListener(v -> selectPaymentMethod(false));
        paymentOnline.setOnClickListener(v -> selectPaymentMethod(true));
        paymentCashOnDelivery.setOnClickListener(v -> selectPaymentMethod(false));
    }

    // Xu ly thanh toan.
    private void handlePayment() {
        if (cartManager.isEmpty()) {
            Toast.makeText(this, "Gio hang trong", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String paymentMethod = rbOnlinePayment.isChecked()
                ? "Thanh toan bang ma QR"
                : "Thanh toan khi nhan hang";

        Toast.makeText(this, "Dang xu ly " + paymentMethod, Toast.LENGTH_SHORT).show();

        User currentUser = authManager.getCurrentUser();
        int userId = currentUser == null ? 0 : currentUser.getId();
        double subtotal = cartManager.getTotalPrice();
        double shippingFee = cartManager.getShippingFee();
        double discount = cartManager.getDiscount();
        String promoCode = cartManager.getAppliedPromoCode();
        double total = subtotal + shippingFee - discount;
        String deliveryAddress = getIntent().getStringExtra("delivery_address");
        long localOrderId = dbHelper.addOrder(
                userId,
                total,
                paymentMethod,
                "Dang xu ly",
                deliveryAddress == null ? "" : deliveryAddress
        );

        saveOrderToFirestore(
                localOrderId,
                currentUser,
                subtotal,
                shippingFee,
                discount,
                promoCode,
                total,
                paymentMethod,
                deliveryAddress == null ? "" : deliveryAddress
        );
    }

    // Luu don len Firestore.
    private void saveOrderToFirestore(long localOrderId,
                                      User currentUser,
                                      double subtotal,
                                      double shippingFee,
                                      double discount,
                                      String promoCode,
                                      double total,
                                      String paymentMethod,
                                      String deliveryAddress) {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        String orderDocumentId = firebaseUser == null
                ? String.valueOf(localOrderId)
                : firebaseUser.getUid() + "_" + localOrderId;
        Map<String, Object> order = new HashMap<>();
        order.put("orderId", localOrderId);
        order.put("orderDocId", orderDocumentId);
        order.put("userId", currentUser == null ? 0 : currentUser.getId());
        order.put("userUid", firebaseUser == null ? "" : firebaseUser.getUid());
        order.put("userEmail", currentUser == null ? "" : currentUser.getEmail());
        order.put("subtotal", subtotal);
        order.put("shippingFee", shippingFee);
        order.put("discount", discount);
        order.put("promoCode", promoCode == null ? "" : promoCode);
        order.put("totalAmount", total);
        order.put("paymentMethod", paymentMethod);
        order.put("deliveryAddress", deliveryAddress);
        order.put("orderStatus", "Dang xu ly");
        order.put("items", buildOrderItems());
        order.put("orderDate", FieldValue.serverTimestamp());
        order.put("createdAt", FieldValue.serverTimestamp());
        order.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseHelper.getFirestore()
                .collection("orders")
                .document(orderDocumentId)
                .set(order)
                .addOnSuccessListener(unused -> incrementVoucherUsageAndFinish(promoCode))
                .addOnFailureListener(error ->
                        Toast.makeText(this,
                                "Loi luu don hang Firebase: " + error.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // Tang luot dung voucher.
    private void incrementVoucherUsageAndFinish(String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            finishPayment();
            return;
        }

        FirebaseHelper.getFirestore()
                .collection("vouchers")
                .document(promoCode.trim().toUpperCase(Locale.ROOT))
                .update("usedCount", FieldValue.increment(1))
                .addOnSuccessListener(unused -> finishPayment())
                .addOnFailureListener(error -> {
                    Toast.makeText(this,
                            "Don hang da luu nhung chua cap nhat luot dung voucher",
                            Toast.LENGTH_SHORT).show();
                    finishPayment();
                });
    }

    // Tao danh sach san pham don.
    private List<Map<String, Object>> buildOrderItems() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem cartItem : cartManager.getCartItems()) {
            Product product = cartItem.getProduct();
            Map<String, Object> item = new HashMap<>();
            item.put("productId", product.getId());
            item.put("productName", product.getName());
            item.put("category", product.getCategory());
            item.put("brand", product.getBrand());
            item.put("price", product.getPrice());
            item.put("quantity", cartItem.getQuantity());
            item.put("selectedSize", cartItem.getSelectedSize());
            item.put("imageUrl", product.getImageUrl());
            item.put("totalPrice", cartItem.getTotalPrice());
            items.add(item);
        }
        return items;
    }

    // Ket thuc thanh toan.
    private void finishPayment() {
        cartManager.clearCart();
        startActivity(new Intent(PaymentActivity.this, OrderSuccessActivity.class));
        finish();
    }

    // Chon cach thanh toan.
    private void selectPaymentMethod(boolean onlinePayment) {
        rbOnlinePayment.setChecked(onlinePayment);
        rbCashOnDelivery.setChecked(!onlinePayment);
        paymentQrContainer.setVisibility(onlinePayment ? View.VISIBLE : View.GONE);
        btnPay.setText(onlinePayment ? "Xac nhan da chuyen khoan" : "Dat hang");
        updateQrInfo(getCurrentTotal());
    }

    // Lay tong tien hien tai.
    private double getCurrentTotal() {
        if (cartManager == null) {
            return 0;
        }
        double shipping = cartManager.isEmpty() ? 0 : cartManager.getShippingFee();
        return cartManager.getTotalPrice() + shipping - cartManager.getDiscount();
    }

    // Cap nhat thong tin QR.
    private void updateQrInfo(double total) {
        if (txtQrInfo == null || rbOnlinePayment == null || !rbOnlinePayment.isChecked()) {
            return;
        }

        String amountText = String.format(new Locale("vi", "VN"), "%,.0f VND", total);
        txtQrInfo.setText("Quet ma QR cua shop de chuyen khoan " + amountText + ". Bam xac nhan sau khi chuyen khoan.");
    }
}
