package com.example.appbanhang;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.appbanhang.firebase.FirestoreRepository;
import com.example.appbanhang.models.Voucher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccountInfoActivity extends AppCompatActivity {
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.account_info_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top + dpToPx(4),
                    systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView title = findViewById(R.id.txt_title);
        content = findViewById(R.id.txt_content);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        String screen = getIntent().getStringExtra("screen");
        title.setText(getTitleForScreen(screen));
        if ("voucher".equals(screen)) {
            loadVouchers();
        } else {
            content.setText(getContentForScreen(screen));
        }
    }

    private void loadVouchers() {
        content.setText("Dang tai ma giam gia tu Firebase...");
        FirestoreRepository.getInstance().fetchVouchers(new FirestoreRepository.VouchersCallback() {
            @Override
            public void onSuccess(List<Voucher> vouchers) {
                List<Voucher> activeVouchers = new ArrayList<>();
                for (Voucher voucher : vouchers) {
                    if (voucher != null && voucher.isActive()
                            && (voucher.getUsageLimit() <= 0
                            || voucher.getUsedCount() < voucher.getUsageLimit())) {
                        activeVouchers.add(voucher);
                    }
                }

                if (activeVouchers.isEmpty()) {
                    content.setText("Chua co ma giam gia kha dung.");
                    return;
                }

                StringBuilder builder = new StringBuilder("Ma giam gia dang hoat dong:\n\n");
                for (Voucher voucher : activeVouchers) {
                    builder.append(voucher.getCode()).append(" - ")
                            .append(safeText(voucher.getTitle(), "Voucher"))
                            .append("\n")
                            .append(buildVoucherSummary(voucher))
                            .append("\n");
                    if (!safeText(voucher.getDescription(), "").isEmpty()) {
                        builder.append(safeText(voucher.getDescription(), "")).append("\n");
                    }
                    if (!safeText(voucher.getEndDate(), "").isEmpty()) {
                        builder.append("Han dung: ").append(voucher.getEndDate()).append("\n");
                    }
                    builder.append("\n");
                }
                content.setText(builder.toString().trim());
            }

            @Override
            public void onError(String errorMessage) {
                content.setText("Khong tai duoc ma giam gia.");
                Toast.makeText(AccountInfoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildVoucherSummary(Voucher voucher) {
        String valueLabel;
        if ("fixed".equalsIgnoreCase(voucher.getType())) {
            valueLabel = "Giam " + formatCurrency(voucher.getValue());
        } else {
            valueLabel = "Giam " + String.format(Locale.US, "%.0f%%", voucher.getValue());
        }

        if (voucher.getMinOrder() > 0) {
            valueLabel += ", don tu " + formatCurrency(voucher.getMinOrder());
        }
        if (voucher.getMaxDiscount() > 0) {
            valueLabel += ", toi da " + formatCurrency(voucher.getMaxDiscount());
        }
        return valueLabel;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String getTitleForScreen(String screen) {
        if ("voucher".equals(screen)) return "Ma giam gia";
        if ("address".equals(screen)) return "Dia chi giao hang";
        if ("faq".equals(screen)) return "Cau hoi thuong gap";
        if ("support".equals(screen)) return "Dich vu khach hang";
        if ("settings".equals(screen)) return "Cai dat";
        if ("terms".equals(screen)) return "Dieu khoan dich vu";
        return "Thong tin";
    }

    private String getContentForScreen(String screen) {
        if ("address".equals(screen)) {
            return "Dia chi giao hang se duoc nhap tai buoc thanh toan. Ban co the cap nhat dia chi moi moi khi dat hang.";
        }
        if ("faq".equals(screen)) {
            return "1. Dat hang nhu the nao?\nChon san pham, them vao gio hang va thanh toan.\n\n"
                    + "2. Gio hang co bi mat khi dong app?\nKhong, gio hang duoc luu theo tai khoan tren Firebase.\n\n"
                    + "3. Trang thai don hang cap nhat o dau?\nTrang thai don hang duoc doc tu Firebase va thay doi theo thao tac cua admin.";
        }
        if ("support".equals(screen)) {
            return "Hotline: 1900 0000\nEmail: support@smarteshop.local\nThoi gian ho tro: 8:00 - 21:00 hang ngay.";
        }
        if ("settings".equals(screen)) {
            return "Phien ban ung dung: 1.0\nDu lieu san pham, gio hang, don hang va ma giam gia duoc dong bo voi Firebase.";
        }
        if ("terms".equals(screen)) {
            return "Khi tao tai khoan Smarteshop, ban dong y cung cap thong tin chinh xac, bao mat mat khau va chi su dung ung dung cho muc dich mua sam hop phap.\n\n"
                    + "Don hang se duoc xu ly theo thong tin giao hang ban nhap tai buoc thanh toan. Ma giam gia co the thay doi theo tung thoi diem.";
        }
        return "Thong tin Smarteshop";
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String formatCurrency(double amount) {
        return String.format(new Locale("vi", "VN"), "%,.0f VND", amount);
    }
}
