package com.example.appbanhang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirebaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.models.Order;
import com.example.appbanhang.models.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderHistoryActivity extends AppCompatActivity {
    private LinearLayout orderContainer;
    private TextView txtEmptyOrders;
    private TextView txtOrderLoading;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;
    private int expandedOrderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.order_history_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top + dpToPx(4),
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        orderContainer = findViewById(R.id.order_container);
        txtEmptyOrders = findViewById(R.id.txt_empty_orders);
        txtOrderLoading = findViewById(R.id.txt_order_loading);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    // Tai don hang.
    private void loadOrders() {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            displayOrders(getLocalOrders());
            return;
        }

        txtEmptyOrders.setVisibility(View.GONE);
        txtOrderLoading.setVisibility(View.VISIBLE);
        orderContainer.removeAllViews();

        FirebaseHelper.getFirestore()
                .collection("orders")
                .whereEqualTo("userUid", firebaseUser.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Order> orders = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        orders.add(toOrder(document));
                    }
                    orders.sort(Comparator.comparing(Order::getOrderDate).reversed());

                    if (orders.isEmpty()) {
                        orders = getLocalOrders();
                    }
                    displayOrders(orders);
                })
                .addOnFailureListener(error -> {
                    Toast.makeText(this,
                            "Khong tai duoc don hang Firebase: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    displayOrders(getLocalOrders());
                });
    }

    // Lay don hang local.
    private List<Order> getLocalOrders() {
        User user = authManager.getCurrentUser();
        int userId = user == null ? 0 : user.getId();
        return dbHelper.getOrders(userId);
    }

    // Hien danh sach don.
    private void displayOrders(List<Order> orders) {
        txtOrderLoading.setVisibility(View.GONE);
        txtEmptyOrders.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
        orderContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (Order order : orders) {
            View row = inflater.inflate(R.layout.item_order, orderContainer, false);
            TextView statusView = row.findViewById(R.id.txt_order_status);
            String status = safeText(order.getOrderStatus(), "Dang xu ly");
            LinearLayout detailContainer = row.findViewById(R.id.order_detail_container);
            boolean expanded = expandedOrderId == order.getOrderId();

            ((TextView) row.findViewById(R.id.txt_order_id)).setText("#" + order.getOrderId());
            statusView.setText(status);
            statusView.setTextColor(getStatusColor(status));
            ((TextView) row.findViewById(R.id.txt_order_date)).setText(
                    "Ngay dat: " + formatter.format(order.getOrderDate()));
            ((TextView) row.findViewById(R.id.txt_order_payment)).setText(
                    "Thanh toan: " + safeText(order.getPaymentMethod(), "Chua co"));
            ((TextView) row.findViewById(R.id.txt_order_total)).setText(
                    String.format(new Locale("vi", "VN"), "%,.0f VND", order.getTotalAmount()));
            ((TextView) row.findViewById(R.id.txt_order_timeline)).setText(buildTimeline(status));
            ((TextView) row.findViewById(R.id.txt_order_address)).setText(
                    "Dia chi: " + safeText(order.getDeliveryAddress(), "Chua co"));
            ((TextView) row.findViewById(R.id.txt_order_money_detail)).setText(buildMoneyDetail(order));
            ((TextView) row.findViewById(R.id.txt_order_items)).setText(buildItemsText(order));
            detailContainer.setVisibility(expanded ? View.VISIBLE : View.GONE);
            row.setOnClickListener(v -> {
                expandedOrderId = expandedOrderId == order.getOrderId() ? -1 : order.getOrderId();
                displayOrders(orders);
            });
            orderContainer.addView(row);
        }
    }

    // Doi Firestore sang Order.
    private Order toOrder(DocumentSnapshot document) {
        int userId = getInt(document, "userId");
        Order order = new Order(userId);
        int orderId = getInt(document, "orderId");
        double totalAmount = getDouble(document, "totalAmount", "total", "amount");
        order.setOrderId(orderId > 0 ? orderId : buildFallbackOrderId(document.getId()));
        order.setOrderStatus(safeText(document.getString("orderStatus"), "Dang xu ly"));
        order.setPaymentMethod(safeText(document.getString("paymentMethod"), "Chua co"));
        order.setDeliveryAddress(safeText(document.getString("deliveryAddress"), ""));
        order.setPromoCode(safeText(document.getString("promoCode"), ""));
        order.setSubtotal(getDouble(document, "subtotal"));
        order.setShippingFee(getDouble(document, "shippingFee"));
        order.setDiscount(getDouble(document, "discount"));
        order.setTotalAmount(totalAmount);
        order.setOrderDate(getDate(document, "orderDate", "createdAt", "updatedAt"));
        order.setItemSummaries(getItemSummaries(document));
        return order;
    }

    // Lay tom tat san pham.
    private List<String> getItemSummaries(DocumentSnapshot document) {
        List<String> summaries = new ArrayList<>();
        Object rawItems = document.get("items");
        if (!(rawItems instanceof List<?>)) {
            return summaries;
        }

        for (Object rawItem : (List<?>) rawItems) {
            if (!(rawItem instanceof Map<?, ?>)) {
                continue;
            }
            Map<?, ?> item = (Map<?, ?>) rawItem;
            String name = safeText(getString(item, "productName", "name"), "San pham");
            String size = safeText(getString(item, "selectedSize", "size"), "");
            int quantity = (int) getNumber(item, "quantity");
            double price = getNumber(item, "price");
            double total = getNumber(item, "totalPrice", "total");
            if (total <= 0) {
                total = price * Math.max(quantity, 1);
            }

            StringBuilder line = new StringBuilder();
            line.append("- ").append(name);
            if (!size.isEmpty()) {
                line.append(" (Size ").append(size).append(")");
            }
            line.append(" x").append(Math.max(quantity, 1));
            line.append(" - ").append(String.format(new Locale("vi", "VN"), "%,.0f VND", total));
            summaries.add(line.toString());
        }
        return summaries;
    }

    // Tao chi tiet tien.
    private String buildMoneyDetail(Order order) {
        Locale locale = new Locale("vi", "VN");
        return "Tam tinh: " + String.format(locale, "%,.0f VND", order.getSubtotal())
                + "\nPhi ship: " + String.format(locale, "%,.0f VND", order.getShippingFee())
                + "\nGiam gia: " + String.format(locale, "%,.0f VND", order.getDiscount())
                + "\nMa giam gia: " + safeText(order.getPromoCode(), "Khong co");
    }

    // Tao text san pham.
    private String buildItemsText(Order order) {
        if (order.getItemSummaries().isEmpty()) {
            return "San pham trong don: Chua co du lieu chi tiet";
        }
        StringBuilder builder = new StringBuilder("San pham trong don:");
        for (String summary : order.getItemSummaries()) {
            builder.append('\n').append(summary);
        }
        return builder.toString();
    }

    // Tao trang thai giao hang.
    private String buildTimeline(String status) {
        String normalized = normalizeStatus(status);
        if (normalized.contains("huy")) {
            return "Dat hang -> Da huy";
        }
        if (normalized.contains("da giao") || normalized.contains("hoan thanh")) {
            return "Dat hang -> Xac nhan -> Dang giao -> Da giao";
        }
        if (normalized.contains("dang giao")) {
            return "Dat hang -> Xac nhan -> Dang giao";
        }
        if (normalized.contains("xac nhan")) {
            return "Dat hang -> Xac nhan";
        }
        return "Dat hang -> Cho xac nhan";
    }

    // Lay chuoi tu map.
    private String getString(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return "";
    }

    // Lay so tu map.
    private double getNumber(Map<?, ?> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                try {
                    return Double.parseDouble(((String) value).trim());
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    // Lay ngay tu Firestore.
    private Date getDate(DocumentSnapshot document, String... fields) {
        for (String field : fields) {
            Date date = document.getDate(field);
            if (date != null) {
                return date;
            }
            Object value = document.get(field);
            if (value instanceof Number) {
                return new Date(((Number) value).longValue());
            }
        }
        return new Date();
    }

    // Lay so nguyen.
    private int getInt(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    // Lay so thuc.
    private double getDouble(DocumentSnapshot document, String... fields) {
        for (String field : fields) {
            Object value = document.get(field);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            if (value instanceof String) {
                try {
                    return Double.parseDouble(((String) value).trim());
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return 0;
    }

    // Lay mau trang thai.
    private int getStatusColor(String status) {
        String normalized = normalizeStatus(status);
        if (normalized.contains("da giao") || normalized.contains("hoan thanh")) {
            return ContextCompat.getColor(this, android.R.color.holo_green_dark);
        }
        if (normalized.contains("huy")) {
            return ContextCompat.getColor(this, R.color.danger);
        }
        return ContextCompat.getColor(this, R.color.primary);
    }

    // Chuan hoa trang thai.
    private String normalizeStatus(String status) {
        String value = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd');
    }

    // Xu ly chuoi rong.
    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    // Tao id don du phong.
    private int buildFallbackOrderId(String documentId) {
        int hash = documentId == null ? 0 : documentId.hashCode();
        return hash == Integer.MIN_VALUE ? Integer.MAX_VALUE : Math.abs(hash);
    }

    // Doi dp sang px.
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
