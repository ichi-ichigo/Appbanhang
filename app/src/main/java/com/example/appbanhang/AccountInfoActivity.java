package com.example.appbanhang;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AccountInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        TextView title = findViewById(R.id.txt_title);
        TextView content = findViewById(R.id.txt_content);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        String screen = getIntent().getStringExtra("screen");
        title.setText(getTitleForScreen(screen));
        content.setText(getContentForScreen(screen));
    }

    private String getTitleForScreen(String screen) {
        if ("voucher".equals(screen)) {
            return "Voucher";
        } else if ("address".equals(screen)) {
            return "Dia chi giao hang";
        } else if ("faq".equals(screen)) {
            return "FAQ";
        } else if ("support".equals(screen)) {
            return "Dich vu khach hang";
        } else if ("settings".equals(screen)) {
            return "Cai dat";
        } else if ("terms".equals(screen)) {
            return "Dieu khoan dich vu";
        }
        return "Thong tin";
    }

    private String getContentForScreen(String screen) {
        if ("voucher".equals(screen)) {
            return "Ma uu dai dang hoat dong:\n\nSALE10 - Giam 10% don hang.\nGIAM10 - Giam 10% don hang.";
        } else if ("address".equals(screen)) {
            return "Dia chi giao hang se duoc nhap tai buoc thanh toan. Ban co the cap nhat dia chi moi khi checkout.";
        } else if ("faq".equals(screen)) {
            return "1. Dat hang nhu the nao?\nChon san pham, them vao gio hang va thanh toan.\n\n2. Gio hang co bi mat khi dong app?\nKhong, gio hang duoc luu theo tai khoan trong SQLite.\n\n3. Wishlist co luu khong?\nCo, danh sach yeu thich duoc luu theo user.";
        } else if ("support".equals(screen)) {
            return "Hotline: 1900 0000\nEmail: support@smarteshop.local\nThoi gian ho tro: 8:00 - 21:00 hang ngay.";
        } else if ("settings".equals(screen)) {
            return "Phien ban ung dung: 1.0\nLuu tru: SQLite local\nDang nhap ghi nho: SharedPreferences.";
        } else if ("terms".equals(screen)) {
            return "Khi tao tai khoan Smarteshop, ban dong y cung cap thong tin chinh xac, bao mat mat khau va chi su dung ung dung cho muc dich mua sam hop phap.\n\nDon hang se duoc xu ly theo thong tin giao hang ban nhap tai buoc checkout. Voucher co the thay doi theo tung thoi diem.\n\nDu lieu tai khoan, gio hang, wishlist va don hang hien duoc luu local bang SQLite tren thiet bi.";
        }
        return "Thong tin Smarteshop";
    }
}
