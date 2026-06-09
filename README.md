# Ứng Dụng Bán Giày - Smarteshop

## 📱 Giới Thiệu
Đây là một ứng dụng di động Android bán giày thể thao với giao diện đẹp, chức năng đầy đủ và trải nghiệm mua sắm mượt mà.

## ✨ Tính Năng Chính

### 1. **Trang Chủ (Home)**
- Hiển thị sản phẩm nổi bật
- Danh mục thương hiệu
- Banner quảng cáo
- Lưới sản phẩm 2 cột

### 2. **Chi Tiết Sản Phẩm**
- Ảnh sản phẩm lớn + thumbnail
- Thông tin giá & mô tả
- Lựa chọn kích cỡ
- Chức năng yêu thích

### 3. **Giỏ Hàng**
- Danh sách sản phẩm
- Chỉnh sửa số lượng
- Tính tổng tiền tự động
- Mã promo

### 4. **Thanh Toán**
- Nhập địa chỉ giao hàng
- Chọn phương thức thanh toán (Online Banking, Thẻ, PayPal)
- Xác nhận đơn hàng

### 5. **Danh Sách Yêu Thích**
- Lưu sản phẩm yêu thích
- Mua nhanh từ wishlist

### 6. **Tài Khoản Người Dùng**
- Thông tin cá nhân
- Lịch sử đơn hàng
- Địa chỉ giao hàng
- Hỗ trợ khách hàng

## 🛠️ Công Nghệ Sử Dụng

- **Ngôn ngữ**: Java/Kotlin
- **Layout**: XML
- **Framework**: Android Native
- **API**: RESTful
- **Database**: Firebase/SQLite (tuỳ chọn)

## 📁 Cấu Trúc Thư Mục

```
app/src/main/
├── java/com/example/appbanhang/
│   ├── MainActivity.java          # Màn hình chính
│   └── ...
├── res/
│   ├── layout/
│   │   ├── activity_main.xml              # Trang chủ
│   │   ├── activity_splash.xml            # Splash screen
│   │   ├── activity_product_detail.xml    # Chi tiết sản phẩm
│   │   ├── activity_add_to_cart.xml       # Thêm vào giỏ
│   │   ├── activity_cart.xml              # Giỏ hàng
│   │   ├── activity_wishlist.xml          # Danh sách yêu thích
│   │   ├── activity_billing_address.xml   # Địa chỉ thanh toán
│   │   ├── activity_payment.xml           # Phương thức thanh toán
│   │   ├── activity_order_success.xml     # Đơn hàng thành công
│   │   ├── activity_account.xml           # Tài khoản người dùng
│   │   ├── item_product.xml               # Item sản phẩm
│   │   ├── item_cart.xml                  # Item giỏ hàng
│   │   └── item_wishlist.xml              # Item yêu thích
│   ├── drawable/
│   │   ├── button_brand_style.xml         # Style nút thương hiệu
│   │   ├── button_size_style.xml          # Style nút kích cỡ
│   │   ├── button_favorite_style.xml      # Style nút yêu thích
│   │   ├── button_delete_style.xml        # Style nút xóa
│   │   ├── button_buy_style.xml           # Style nút mua
│   │   └── edittext_style.xml             # Style input
│   └── values/
│       └── strings.xml                    # String resources
```

## 🎨 Màu Sắc & Design

| Tên Màu | Mã Màu | Sử Dụng |
|---------|--------|--------|
| Cyan/Teal | #00BCD4 | Màu chính (banner, nút) |
| Purple | #8E24AA | Màu phụ |
| Trắng | #FFFFFF | Nền & thẻ |
| Đen | #1A1A1A | Nút chính, text |
| Xám | #999999 | Text phụ |
| Đỏ | #FF6B6B | Giá, cảnh báo |

## 🚀 Cách Bắt Đầu

### 1. Clone/Tải Dự Án
```bash
git clone <repository>
cd Appbanhang
```

### 2. Mở Trong Android Studio
- File → Open → Chọn thư mục Appbanhang

### 3. Build & Run
- Click **Run** hoặc **Shift + F10**
- Chọn emulator hoặc thiết bị kết nối

## 📝 Ghi Chú Phát Triển

### Layout Principles
- Responsive design cho tất cả kích cỡ màn hình
- ScrollView cho nội dung dài
- RecyclerView cho danh sách
- Bottom navigation cho điều hướng chính

### Naming Conventions
- Activity: `activity_*.xml`
- Item layouts: `item_*.xml`
- Drawable styles: `*_style.xml`
- Tất cả text dùng Vietnamese (Tiếng Việt)

### Tiếp Theo
1. Tạo Activity Java classes
2. Kết nối RecyclerView Adapters
3. Tích hợp API/Database
4. Tạo Data Models
5. Thêm Logic & Navigation

## 📞 Liên Hệ & Hỗ Trợ

Xem file **PLAN.md** để xem chi tiết kế hoạch phát triển và trạng thái dự án.

---
**Ngôn Ngữ**: Tiếng Việt  
**Platform**: Android  
**API Level**: 24+  
**Target Version**: Android 12+
