# 📊 Vị Trí Lưu Trữ Dữ Liệu trong App

## 🗄️ Tổng Quan Hệ Thống Lưu Trữ

```
┌─────────────────────────────────────────────────────┐
│           DỮ LIỆU TRONG ỨNG DỤNG                    │
├─────────────────────────────────────────────────────┤
│                                                     │
│  1. DATABASE (SQLite)                               │
│     └─ File: smarteshop.db (persistent)            │
│                                                     │
│  2. MEMORY (RAM - mất khi đóng app)                 │
│     ├─ CartManager (giỏ hàng)                      │
│     ├─ WishlistManager (danh sách yêu thích)      │
│     └─ AuthManager (user hiện tại)                │
│                                                     │
│  3. SHARED PREFERENCES (Optional)                   │
│     └─ Chưa sử dụng hiện tại                       │
│                                                     │
└─────────────────────────────────────────────────────┘
```

---

## 🔍 CHI TIẾT TỪNG NƠI LƯU TRỮ

### 1️⃣ DATABASE SQLite - PERSISTENT STORAGE

**📂 Vị trí file:** 
```
/data/data/com.example.appbanhang/databases/smarteshop.db
```

**Class quản lý:** 
```
→ DatabaseHelper.java
  Vị trí: app/src/main/java/com/example/appbanhang/database/DatabaseHelper.java
```

**Các bảng dữ liệu:**

#### **Bảng: `users` (Đăng nhập & Đăng kí)**
```
TABLE_USERS:
├── id              (INTEGER PRIMARY KEY)
├── full_name       (TEXT)
├── email           (TEXT UNIQUE)
├── phone_number    (TEXT)
└── password        (TEXT)

📍 Lưu: Thông tin tài khoản đăng nhập/đăng kí
🔐 Kiểm tra: AuthManager → dbHelper.verifyUser(email, password)
```

**Dữ liệu mẫu lưu trong code:**
```java
// File: AuthManager.java (line 13-16)
userDatabase.put("sultan@example.com", "password123");
userDatabase.put("user@example.com", "user123");
```

---

#### **Bảng: `products` (Danh sách sản phẩm)**
```
TABLE_PRODUCTS:
├── id              (INTEGER PRIMARY KEY)
├── name            (TEXT)
├── category        (TEXT)
├── price           (REAL)
├── image_url       (TEXT)
├── description     (TEXT)
├── rating          (REAL)
└── brand           (TEXT)

📍 Lưu: Tất cả sản phẩm có sẵn
🔄 Được load vào app khi mở MainActivity
```

---

#### **Bảng: `favorites` (Danh sách yêu thích - trong DB)**
```
TABLE_FAVORITES:
├── id              (INTEGER PRIMARY KEY)
├── user_id         (INTEGER)
├── product_id      (INTEGER)
└── created_date    (TEXT)

📍 Lưu: Sản phẩm yêu thích của từng user
```

---

#### **Bảng: `orders` (Lịch sử đơn hàng)**
```
TABLE_ORDERS:
├── order_id        (INTEGER PRIMARY KEY)
├── user_id         (INTEGER)
├── total_amount    (REAL)
├── order_status    (TEXT)
├── payment_method  (TEXT)
└── order_date      (TEXT)

📍 Lưu: Lịch sử các đơn hàng đã đặt
```

---

### 2️⃣ MEMORY (RAM) - TEMPORARY STORAGE - ⚠️ MẤT KHI ĐÓNG APP

#### **CartManager (Giỏ hàng)**

**Class:** 
```
→ CartManager.java
  Vị trí: app/src/main/java/com/example/appbanhang/managers/CartManager.java
```

**Lưu ở đâu:**
```java
private List<CartItem> cartItems;        // Cache trong RAM
DatabaseHelper.saveCartItem(...)        // Lưu bền vào SQLite
private double subtotal;
private double shippingFee = 12000;
private double discount = 0;
```

**✅ Trạng thái hiện tại:**
- Giỏ hàng được cache trong RAM để UI cập nhật nhanh
- Khi user đã đăng nhập, cart item được lưu vào bảng `cart_items`
- Khi mở app/cart/payment, app đồng bộ lại từ SQLite theo `user_id`

**📌 Cách sử dụng:**
```java
// Thêm vào giỏ
CartManager.getInstance().addToCart(product, quantity, size);

// Lấy giỏ hàng
List<CartItem> items = CartManager.getInstance().getCartItems();

// Xóa item
CartManager.getInstance().removeFromCart(item);

// Xóa tất cả
CartManager.getInstance().clearCart();
```

**Dữ liệu được lưu:**
```
CartItem:
├── product (Product object)
├── quantity (số lượng)
└── selectedSize (kích cỡ chọn)
```

---

#### **WishlistManager (Danh sách yêu thích)**

**Class:** 
```
→ WishlistManager.java
  Vị trí: app/src/main/java/com/example/appbanhang/managers/WishlistManager.java
```

**Lưu ở đâu:**
```java
private List<Product> wishlistItems;     // Cache trong RAM
DatabaseHelper.addToFavorites(...)       // Lưu bền vào SQLite
```

**✅ Trạng thái hiện tại:**
- Danh sách yêu thích được cache trong RAM để hiển thị nhanh
- Khi user đã đăng nhập, favorite được lưu vào bảng `favorites`
- Khi mở wishlist/home, app đồng bộ lại từ SQLite theo `user_id`

**📌 Cách sử dụng:**
```java
// Thêm vào wishlist
WishlistManager.getInstance().addToWishlist(product);

// Lấy wishlist
List<Product> favorites = WishlistManager.getInstance().getWishlistItems();

// Xóa khỏi wishlist
WishlistManager.getInstance().removeFromWishlist(product);

// Kiểm tra có trong wishlist không
boolean isFavorite = WishlistManager.getInstance().isInWishlist(productId);
```

---

#### **AuthManager (User đang đăng nhập)**

**Class:** 
```
→ AuthManager.java
  Vị trí: app/src/main/java/com/example/appbanhang/managers/AuthManager.java
```

**Lưu ở đâu:**
```java
private User currentUser;                // ← Lưu trong RAM (user hiện tại)
private Map<String, String> userDatabase; // ← Tài khoản mẫu trong code
private static DatabaseHelper dbHelper;   // ← Kết nối đến database
```

**📌 Cách sử dụng:**
```java
// Đăng nhập
AuthManager.getInstance().login("email@example.com", "password");

// Lấy user hiện tại
User currentUser = AuthManager.getInstance().getCurrentUser();

// Đăng ký
AuthManager.getInstance().register("new@example.com", "pass123", "Tên Người Dùng");

// Đăng xuất
AuthManager.getInstance().logout();
```

---

### 3️⃣ SHARED PREFERENCES (Chưa sử dụng)

**Có thể dùng để:**
```
- Lưu theme (sáng/tối)
- Lưu ngôn ngữ ưa thích
- Lưu token đăng nhập
- Lưu cấu hình người dùng
```

**Cách dùng (nếu muốn):**
```java
SharedPreferences prefs = context.getSharedPreferences("app_prefs", MODE_PRIVATE);

// Lưu
prefs.edit().putString("key", "value").apply();

// Lấy
String value = prefs.getString("key", "default");
```

---

## 📋 BẢNG TÓMLƯỢC

| Dữ Liệu | Nơi Lưu | Persistent? | File/Class | Ghi Chú |
|---------|---------|-------------|-----------|--------|
| **User (Login/Register)** | SQLite Database | ✅ Có | DatabaseHelper + AuthManager | Lưu email, password, họ tên |
| **Danh sách sản phẩm** | SQLite Database | ✅ Có | DatabaseHelper | Giá, ảnh, thông tin |
| **Giỏ hàng (Cart)** | SQLite + RAM cache | ✅ Có | CartManager + DatabaseHelper | Lưu theo user_id |
| **Danh sách yêu thích** | SQLite + RAM cache | ✅ Có | WishlistManager + DatabaseHelper | Lưu theo user_id |
| **Lịch sử đơn hàng** | SQLite Database | ✅ Có | DatabaseHelper | Bảng `orders` |
| **Sản phẩm yêu thích (DB)** | SQLite Database | ✅ Có | DatabaseHelper | Bảng `favorites` |

---

## ⚠️ VẤNS ĐỀ CẦN BIẾT

### ✅ Problem 1: Giỏ Hàng Mất Khi Đóng App
```
✓ Đã xử lý: CartManager sync với SQLite table `cart_items` theo user_id
```

### ✅ Problem 2: Danh Sách Yêu Thích Mất Khi Đóng App
```
✓ Đã xử lý: WishlistManager sync với SQLite table `favorites` cho từng user
```

### 🔴 Problem 3: User Hiện Tại Mất Khi Đóng App
```
✗ Hiện tại: AuthManager chỉ lưu currentUser trong RAM
✓ Nên là: Dùng SharedPreferences lưu user_id hoặc token
```

---

## 📍 CÁCH TÌM FILE TRONG ANDROID STUDIO

### Xem SQLite Database:
```
1. Mở Android Studio
2. View → Tool Windows → Device File Explorer
3. Điều hướng đến: /data/data/com.example.appbanhang/databases/
4. Tìm file: smarteshop.db
5. Kéo thả vào project để xem
```

### Xem Code Quản Lý Dữ Liệu:
```
1. DatabaseHelper.java
   → app > java > com.example.appbanhang > database > DatabaseHelper.java

2. AuthManager.java
   → app > java > com.example.appbanhang > managers > AuthManager.java

3. CartManager.java
   → app > java > com.example.appbanhang > managers > CartManager.java

4. WishlistManager.java
   → app > java > com.example.appbanhang > managers > WishlistManager.java
```

---

## 💾 COMMAND LẤY DỮ LIỆU TỪ DATABASE

Nếu muốn xem dữ liệu từ command line:

```bash
# Kết nối đến device
adb shell

# Vào thư mục database
cd /data/data/com.example.appbanhang/databases/

# Mở SQLite
sqlite3 smarteshop.db

# Xem các bảng
.tables

# Xem dữ liệu users
SELECT * FROM users;

# Xem dữ liệu products
SELECT * FROM products;

# Xem dữ liệu orders
SELECT * FROM orders;

# Thoát
.exit
```

---

## 🎯 TÓMLƯỢC NHANH

| Câu Hỏi | Câu Trả Lời |
|--------|-----------|
| **Dữ liệu đăng nhập lưu ở đâu?** | SQLite Database (bảng `users`) + RAM (AuthManager) |
| **Giỏ hàng lưu ở đâu?** | SQLite bảng `cart_items` + RAM cache trong CartManager |
| **Danh sách yêu thích lưu ở đâu?** | SQLite bảng `favorites` + RAM cache trong WishlistManager |
| **Sản phẩm lưu ở đâu?** | SQLite Database (bảng `products`) |
| **Lịch sử mua hàng lưu ở đâu?** | SQLite Database (bảng `orders`) |
| **File database ở đâu?** | `/data/data/com.example.appbanhang/databases/smarteshop.db` |
| **Class nào quản lý database?** | DatabaseHelper.java |
| **Làm sao để giỏ hàng không mất?** | Đã lưu vào SQLite bảng `cart_items` theo user_id |

# Cap nhat 2026-06-09

- Password co the duoc dat lai bang ForgotPasswordActivity neu email da ton tai trong SQLite.
- Ho so user co the cap nhat ho ten/so dien thoai bang EditProfileActivity.
- Google/Facebook login trong ban hien tai la local demo provider account, chua phai OAuth Firebase/Google/Facebook that.
- SharedPreferences hien dang duoc su dung de ghi nho email dang nhap khi user tick "Ghi nho toi".
- `AuthManager.restoreSession(email)` khoi phuc user tu SQLite khi mo lai app.
- `cart_items` luu gio hang theo `user_id`.
- `favorites` luu wishlist theo `user_id`.
- `orders` luu lich su don hang, tong tien, trang thai, phuong thuc thanh toan va dia chi giao hang.
- `products` duoc seed tu `DataProvider` vao SQLite neu database rong.
- `banners` va `brands` da co schema trong SQLite de san sang thay mock data bang du lieu dong.

Firebase chua phai backend dang chay cua app hien tai. App van luu local bang SQLite vi chua co `app/google-services.json` va chua bat Google Services plugin.
