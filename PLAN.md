# Kế Hoạch Dự Án - Ứng Dụng Bán Giày Smarteshop

## 📋 Tổng Quan Dự Án
Xây dựng ứng dụng di động bán giày thể thao (Smarteshop) với giao diện thân thiện, tính năng mua hàng hoàn chỉnh và quản lý tài khoản người dùng.

## 🎯 Mục Tiêu Chính
- Tạo ứng dụng bán hàng giày hoàn chỉnh
- Cung cấp trải nghiệm mua sắm mượt mà
- Tích hợp thanh toán online
- Quản lý giỏ hàng và danh sách yêu thích

## 📱 Các Màn Hình Chính

### 0. **Login Screen** (Màn hình đăng nhập)
- Nhập Email
- Nhập Mật khẩu
- Checkbox "Ghi nhớ tôi"
- Nút "Quên mật khẩu?"
- Nút "Đăng Nhập"
- Đăng nhập bằng Google & Facebook
- Link "Đăng Kí" cho người dùng mới

### 1. **Register Screen** (Màn hình đăng kí)
- Nhập Họ và Tên
- Nhập Email
- Nhập Số Điện Thoại
- Nhập Mật khẩu
- Xác nhận Mật khẩu
- Checkbox chấp nhận Điều khoản & Dịch vụ
- Nút "Đăng Kí"
- Link "Đăng Nhập" cho người dùng có tài khoản

### 2. **Splash Screen** (Màn hình khởi động)
- Logo Smarteshop
- Thiết kế đơn giản, tập trung vào thương hiệu
- Thời gian hiển thị: 2-3 giây

### 3. **Home Screen** (Trang chủ)
- Lời chào người dùng (Hi, Sultan! Welcome back)
- Khung quảng cáo chính (Biaya Sepatu Rebook - 15% discount)
- Danh mục thương hiệu (Brands): Nike, Puma, Adidas, Nike
- Danh sách sản phẩm (grid 2 cột):
  - Hình ảnh sản phẩm
  - Tên sản phẩm
  - Giá
  - Rating
- Bottom navigation: Home, Search, Cart, Wishlist, Account

### 4. **Promotion/Search Screen** (Khuyến mãi & Tìm kiếm)
- Thanh tìm kiếm
- Banner quảng cáo (Sport Collection 30% OFF, Shoes Collection 15% OFF)

### 5. **Product Detail Screen** (Chi tiết sản phẩm)
- Ảnh sản phẩm lớn
- Thumbnail ảnh
- Tên sản phẩm
- Giá tiền
- Mô tả sản phẩm
- Lựa chọn kích cỡ (Size selection)
- Nút "Beli Sekarang" (Mua Ngay)
- Icon yêu thích

### 6. **Add to Cart Screen** (Thêm vào giỏ hàng)
- Ảnh sản phẩm
- Tên sản phẩm
- Giá tiền
- Chọn size
- Chọn số lượng
- Nút "Tambahkan Keranjang" (Thêm vào Giỏ)

### 7. **Cart Screen** (Giỏ hàng)
- Danh sách sản phẩm trong giỏ
- Thông tin giá:
  - Sub-total (Tổng phụ)
  - Shipping fee (Biaya Pengiriman)
  - Total (Tổng cộng)
- Nút "Checkout" (Thanh toán)
- Mã Promo

### 8. **Wishlist Screen** (Danh sách yêu thích)
- Danh sách sản phẩm yêu thích
- Nút "Beli Sekarang" cho từng sản phẩm

### 9. **Billing Address Screen** (Địa chỉ thanh toán)
- Nhập Nama Lengkap (Họ và Tên)
- Nhập Alamat (Địa chỉ)
- Chọn Provinsi (Tỉnh thành)
- Nhập Pos Code (Mã bưu điện)
- Chọn Negara (Quốc gia)
- Chọn Opsi Pengiriman (Tùy chọn vận chuyển)
- Checkbox tiếp nhận thông báo

### 10. **Payment Screen** (Phương thức thanh toán)
- Danh sách phương thức thanh toán:
  - Online Banking (FPX)
  - Credit / Debit Card (Visa, MasterCard)
  - PayPal
- Tổng cộng hiển thị
- Nút "Pesan" (Đặt hàng)

### 11. **Success Screen** (Thành công)
- Icon giỏ hàng với dấu tích
- Thông báo "Pesanan Sukses"
- Lưu ý thời gian xử lý đơn hàng
- Nút "Kembali Belanja" (Tiếp tục mua sắm)

### 12. **Account Screen** (Tài khoản)
- Thông tin cá nhân:
  - Avatar người dùng
  - Tên người dùng
- Thống kê:
  - Pengambilan (Lượt lấy)
  - Dikirimi (Lượt gửi)
  - Siap untuk Pickup (Sẵn sàng lấy)
- Menu khác:
  - Order Saya (Đơn hàng của tôi)
  - Voucher
  - Alamat Pengiriman (Địa chỉ giao hàng)
  - FAQ
  - Pelayanan Pelanggan (Dịch vụ khách hàng)
  - Pengaturan (Cài đặt)

## 🛠️ Công Nghệ Sử Dụng
- **Ngôn ngữ**: Java / Kotlin
- **Framework**: Android Native
- **Layout**: XML
- **Database**: Firebase / SQLite
- **API Payment**: Stripe / PayPal / Local Bank

## 📊 Luồng Dữ Liệu
1. **Người dùng** → Xem sản phẩm
2. Chọn sản phẩm → Chi tiết
3. Thêm vào giỏ → Giỏ hàng
4. Checkout → Địa chỉ
5. Chọn thanh toán → Xác nhận
6. Thành công → Quay về

## ✅ Danh Sách Công Việc

### Phase 1: UI/UX - Layout XML ✓ HOÀN THÀNH
- [x] Tạo activity_login.xml
- [x] Tạo activity_register.xml
- [x] Tạo activity_splash.xml
- [x] Tạo activity_home.xml
- [x] Tạo activity_product_detail.xml
- [x] Tạo activity_add_to_cart.xml
- [x] Tạo activity_cart.xml
- [x] Tạo activity_wishlist.xml
- [x] Tạo activity_billing_address.xml
- [x] Tạo activity_payment.xml
- [x] Tạo activity_order_success.xml
- [x] Tạo activity_account.xml
- [x] Tạo item_product.xml (RecyclerView item)
- [x] Tạo item_cart.xml (RecyclerView item)
- [x] Tạo item_wishlist.xml (RecyclerView item)
- [x] Tạo drawable styles (button & edittext)
- [x] Cập nhật activity_main.xml với Home Screen

### Phase 2: Logic & Functionality ✓ HOÀN THÀNH
- [x] Tạo LoginActivity.java
- [x] Tạo RegisterActivity.java
- [x] Tạo MainActivity.java
- [x] Tạo Product Model
- [x] Tạo User Model
- [x] Tạo CartItem Model
- [x] Tạo Order Model
- [x] Tạo CartManager
- [x] Tạo AuthManager
- [x] Tạo WishlistManager
- [x] Tạo DatabaseHelper (SQLite)
- [x] Tạo ProductAdapter
- [x] Tạo CartAdapter
- [x] Tạo WishlistAdapter

### Phase 3: Integration ✓ HOÀN THÀNH
- [x] Tạo SplashActivity (Entry point)
- [x] Tạo ProductDetailActivity
- [x] Tạo CartActivity
- [x] Tạo BillingAddressActivity
- [x] Tạo PaymentActivity
- [x] Tạo OrderSuccessActivity
- [x] Tạo WishlistActivity
- [x] Tạo AccountActivity
- [x] Cài đặt ProductAdapter cho RecyclerView
- [x] Kết nối navigation giữa các Activity
- [x] Cập nhật CartManager (addToCart, getTotalPrice, getAllItems, clearCart)
- [x] Cập nhật AndroidManifest.xml với tất cả Activity
- [x] Mock Payment Gateway

### Phase 4: Bug Fixes & Optimization
- [x] Fix RecyclerView crash (androidx.recyclerview.widget)
- [x] Fix gravity value (center_vertical|space_around → center_vertical)
- [x] Fix strokeWidth on MaterialButton (1dp)
- [x] Update AuthManager with DatabaseHelper for persistent storage
- [x] Update DatabaseHelper with user authentication methods
- [ ] Test all flows end-to-end
- [ ] Fix any remaining runtime errors

## 🎨 Màu Sắc & Styling
- **Màu chính**: Teal/Cyan (#00BCD4)
- **Màu phụ**: Purple/Violet (#8E24AA)
- **Màu nền**: Trắng (#FFFFFF)
- **Màu văn bản**: Đen (#000000), Xám (#999999)
- **Màu nút**: Đen (#1A1A1A)

## 📝 Ghi Chú
- Bao gồm tiếng Việt cho tất cả text
- Responsive design cho tất cả kích cỡ màn hình
- Hỗ trợ Dark Mode (tuỳ chọn)
- Smooth animations & transitions

---
**Cập nhật lần cuối**: June 8, 2026
**Trạng thái**: Phase 1, 2 & 3 Hoàn Thành ✓
