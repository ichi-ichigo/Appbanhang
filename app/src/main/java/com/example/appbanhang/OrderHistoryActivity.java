package com.example.appbanhang;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.managers.AuthManager;
import com.example.appbanhang.models.Order;
import com.example.appbanhang.models.User;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderHistoryActivity extends AppCompatActivity {
    private LinearLayout orderContainer;
    private TextView txtEmptyOrders;
    private DatabaseHelper dbHelper;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        dbHelper = new DatabaseHelper(this);
        authManager = AuthManager.getInstance();
        orderContainer = findViewById(R.id.order_container);
        txtEmptyOrders = findViewById(R.id.txt_empty_orders);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        loadOrders();
    }

    private void loadOrders() {
        User user = authManager.getCurrentUser();
        int userId = user == null ? 0 : user.getId();
        List<Order> orders = dbHelper.getOrders(userId);

        txtEmptyOrders.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
        orderContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (Order order : orders) {
            View row = inflater.inflate(R.layout.item_order, orderContainer, false);
            ((TextView) row.findViewById(R.id.txt_order_id)).setText("#" + order.getOrderId());
            ((TextView) row.findViewById(R.id.txt_order_status)).setText(order.getOrderStatus());
            ((TextView) row.findViewById(R.id.txt_order_date)).setText(
                    "Ngay dat: " + formatter.format(order.getOrderDate()));
            ((TextView) row.findViewById(R.id.txt_order_payment)).setText(
                    "Thanh toan: " + order.getPaymentMethod());
            ((TextView) row.findViewById(R.id.txt_order_total)).setText(
                    String.format("Rp. %.0f", order.getTotalAmount()));
            orderContainer.addView(row);
        }
    }
}
