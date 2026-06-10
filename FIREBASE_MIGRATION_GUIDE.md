# 🔥 Giải Pháp: Chuyển Sang Firebase Thay Vì SQLite

## 🎯 Tại Sao Nên Dùng Firebase?

| Tiêu Chí | SQLite | Firebase |
|----------|--------|----------|
| **Sync dữ liệu** | Thủ công | ✅ Real-time |
| **Backup** | Tự quản lý | ✅ Tự động |
| **Scale** | Giới hạn | ✅ Vô hạn |
| **Xác thực** | Tự code | ✅ Built-in |
| **Offline** | ❌ Khó | ✅ Có sẵn |
| **Multi-device sync** | ❌ Khó | ✅ Tự động |

---

## 📦 Firebase Services Cần Dùng

```
Firebase Console (console.firebase.google.com)
│
├─ 🔐 Authentication (Đăng nhập/Đăng kí)
│   ├─ Email & Password
│   ├─ Google Sign-In
│   └─ Facebook Login
│
├─ 📊 Realtime Database / Firestore (Dữ liệu)
│   ├─ Users
│   ├─ Products
│   ├─ Cart Items
│   ├─ Wishlist
│   └─ Orders
│
├─ 🖼️ Cloud Storage (Hình ảnh)
│   ├─ Product images
│   ├─ Banner images
│   └─ User avatars
│
└─ 📨 Cloud Messaging (Optional - Thông báo)
    └─ Push notifications
```

---

## 🔄 So Sánh: SQLite → Firebase

### **Hiện Tại (SQLite):**
```
Android App
    ↓
DatabaseHelper (SQLite)
    ↓
smarteshop.db (Local Storage)
```

### **Sau Khi Chuyển (Firebase):**
```
Android App
    ↓
Firebase SDK
    ├─ Firebase Authentication
    ├─ Cloud Firestore (Database)
    ├─ Cloud Storage (Images)
    └─ Realtime Database (Optional)
         ↓
    Firebase Console (Backend)
         ↓
    Cloud (Google Servers)
```

---

## 🛠️ BƯỚC 1: Setup Firebase Project

### Step 1.1: Tạo Firebase Project

```
1. Truy cập: https://console.firebase.google.com
2. Click "Create a new project"
3. Tên project: "Smarteshop"
4. Chọn quốc gia: Vietnam
5. Bỏ check "Enable Google Analytics" (optional)
6. Click "Create project"
7. Chờ setup xong (~ 1-2 phút)
```

### Step 1.2: Register Android App

```
1. Tại Firebase Console
2. Click "Add app" → Android
3. Nhập:
   - Android package name: com.example.appbanhang
   - App nickname: Smarteshop
   - SHA-1 certificate: (xem bước dưới)

4. Lấy SHA-1 Certificate:
   - Mở Android Studio
   - Gradle (bên phải) → Tasks → android → signingReport
   - Double click → Lấy SHA1
   - Paste vào Firebase
   
5. Click "Next"
6. Download google-services.json
7. Đặt file vào: app/ folder
8. Click "Next" → "Finish"
```

---

## 📦 BƯỚC 2: Add Firebase Dependencies

### Step 2.1: Sửa `build.gradle.kts` (Project level)

**File:** `build.gradle.kts` (ở root)

```gradle
plugins {
    // ... existing plugins
    id("com.google.gms.google-services") version "4.4.0" apply false
}
```

### Step 2.2: Sửa `app/build.gradle.kts` (App level)

**File:** `app/build.gradle.kts`

```gradle
plugins {
    id("com.android.application")
    id("com.google.gms.google-services")  // ← ADD THIS
}

dependencies {
    // Firebase Core
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    
    // Firebase Firestore (Database)
    implementation("com.google.firebase:firebase-firestore")
    
    // Firebase Realtime Database (Alternative)
    implementation("com.google.firebase:firebase-database")
    
    // Firebase Cloud Storage (Images)
    implementation("com.google.firebase:firebase-storage")
    
    // Coroutines for async tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
}
```

---

## 🔐 BƯỚC 3: Setup Firebase Authentication

### Step 3.1: Enable Authentication Methods

```
Firebase Console:
1. Project Settings → Authentication
2. Sign-in method
3. Enable:
   ✓ Email/Password
   ✓ Google (ID: từ Google Cloud Console)
   ✓ Facebook (App ID: từ Facebook Developer)
```

### Step 3.2: Cách Dùng Trong Code (Không cần sửa code)

**Thay vì:**
```java
// SQLite
AuthManager.login("email@example.com", "password123");
```

**Sẽ thành:**
```java
// Firebase
FirebaseAuth.getInstance()
    .signInWithEmailAndPassword("email@example.com", "password123")
    .addOnSuccessListener(authResult -> {
        FirebaseUser user = authResult.getUser();
        // Success
    })
    .addOnFailureListener(e -> {
        // Error
    });
```

---

## 📊 BƯỚC 4: Setup Firestore Database

### Step 4.1: Enable Firestore

```
Firebase Console:
1. Firestore Database
2. Click "Create database"
3. Production mode hoặc Test mode (dùng Test mode lúc dev)
4. Location: asia-southeast1 (Việt Nam)
5. Click "Create"
```

### Step 4.2: Collection Structure

```
Firestore Database:
│
├─ users/ (Collection)
│   └─ {userId}/ (Document)
│       ├─ fullName: "Võ Minh Vinh"
│       ├─ email: "vinh@example.com"
│       ├─ phone: "0901234567"
│       ├─ createdAt: 1717862400000
│       └─ address: "123 Main St"
│
├─ products/ (Collection)
│   └─ {productId}/ (Document)
│       ├─ name: "Air Jordan Retro"
│       ├─ price: 299.99
│       ├─ category: "Sneakers"
│       ├─ imageUrl: "gs://bucket/product1.jpg"
│       ├─ brand: "Nike"
│       ├─ rating: 4.8
│       └─ stock: 45
│
├─ cart/ (Collection)
│   └─ {userId}/ (Document)
│       └─ items/ (Sub-collection)
│           └─ {itemId}/ (Document)
│               ├─ productId: "123"
│               ├─ quantity: 2
│               ├─ size: "M"
│               └─ addedAt: 1717862400000
│
├─ wishlist/ (Collection)
│   └─ {userId}/ (Document)
│       └─ items/ (Sub-collection)
│           └─ {productId}/ (Document)
│               ├─ addedAt: 1717862400000
│               └─ rating: 5
│
└─ orders/ (Collection)
    └─ {orderId}/ (Document)
        ├─ userId: "user123"
        ├─ items: [...]
        ├─ totalAmount: 599.98
        ├─ status: "pending"
        ├─ paymentMethod: "card"
        └─ createdAt: 1717862400000
```

---

## 🖼️ BƯỚC 5: Setup Cloud Storage (Ảnh)

### Step 5.1: Enable Cloud Storage

```
Firebase Console:
1. Storage
2. Click "Get started"
3. Security rules: Test mode
4. Location: asia-southeast1
5. Click "Done"
```

### Step 5.2: Upload Ảnh

```
Structure trong Storage:
gs://smarteshop-bucket/
│
├─ products/
│   ├─ product_1.jpg
│   ├─ product_2.jpg
│   └─ ...
│
├─ banners/
│   ├─ banner_1.jpg
│   ├─ banner_2.jpg
│   └─ ...
│
└─ users/
    ├─ user_123_avatar.jpg
    └─ ...
```

---

## 💻 BƯỚC 6: Tạo Firebase Helper Class (Không sửa code cũ)

### Tạo file mới: `FirebaseHelper.java`

```java
package com.example.appbanhang.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

public class FirebaseHelper {
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore mFirestore;
    private static FirebaseStorage mStorage;

    public static FirebaseAuth getAuth() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }
        return mAuth;
    }

    public static FirebaseFirestore getFirestore() {
        if (mFirestore == null) {
            mFirestore = FirebaseFirestore.getInstance();
        }
        return mFirestore;
    }

    public static FirebaseStorage getStorage() {
        if (mStorage == null) {
            mStorage = FirebaseStorage.getInstance();
        }
        return mStorage;
    }
}
```

---

## 🔄 BƯỚC 7: Mapping Dữ Liệu

### Thay Thế DatabaseHelper

**Cũ:**
```java
// DatabaseHelper.java
public boolean verifyUser(String email, String password) {
    // SQLite query
}
```

**Mới:**
```java
// FirebaseAuthService.java (tạo mới)
public void verifyUser(String email, String password, 
                      OnAuthListener listener) {
    FirebaseHelper.getAuth()
        .signInWithEmailAndPassword(email, password)
        .addOnSuccessListener(task -> {
            listener.onSuccess(task.getUser());
        })
        .addOnFailureListener(e -> {
            listener.onError(e.getMessage());
        });
}

public interface OnAuthListener {
    void onSuccess(FirebaseUser user);
    void onError(String error);
}
```

---

## 🔑 BƯỚC 8: Security Rules (Firestore)

### Đặt trong Firestore Console → Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users có thể đọc/sửa dữ liệu của mình
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Products ai cũng có thể đọc
    match /products/{document=**} {
      allow read: if true;
      allow write: if request.auth.uid == 'admin_uid';
    }
    
    // Cart chỉ user đó có thể đọc/sửa
    match /cart/{userId}/{document=**} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Wishlist
    match /wishlist/{userId}/{document=**} {
      allow read, write: if request.auth.uid == userId;
    }
    
    // Orders
    match /orders/{orderId} {
      allow read: if request.auth.uid == resource.data.userId;
      allow create: if request.auth.uid != null;
    }
  }
}
```

---

## 📈 BƯỚC 9: Lợi Ích Ngay Lập Tức

### ✅ Mà Không Cần Sửa Code Cũ

| Tính Năng | Lợi Ích |
|-----------|---------|
| **Real-time Sync** | Giỏ hàng tự động cập nhật trên nhiều device |
| **Offline Support** | Dữ liệu vẫn dùng được khi offline |
| **Automatic Backup** | Firebase tự động backup 24/7 |
| **Scale Vô Hạn** | Có thể xử lý triệu user |
| **Security** | Firebase xác thực & bảo vệ tự động |
| **Analytics** | Tự động track user behavior |
| **No Server** | Không phải setup server riêng |

---

## ⏳ BƯỚC 10: Timeline Chuyển Đổi

### Phase 1: Setup (1 ngày)
```
[x] Tạo Firebase Project
[x] Thêm dependencies
[x] Enable Authentication
[x] Enable Firestore
[x] Enable Cloud Storage
```

### Phase 2: Auth (1 ngày)
```
[x] Viết Firebase Auth Service
[x] Test login/register
[x] Kiểm tra user được lưu
```

### Phase 3: Database (2 ngày)
```
[x] Viết Firestore Service cho Products
[x] Viết Service cho Cart
[x] Viết Service cho Wishlist
[x] Viết Service cho Orders
```

### Phase 4: Storage (1 ngày)
```
[x] Upload ảnh lên Cloud Storage
[x] Update image URLs
[x] Test load ảnh
```

### Phase 5: Migration (2 ngày)
```
[x] Migrate dữ liệu cũ → Firebase
[x] Update Android App code
[x] Test toàn bộ flow
[x] Deploy
```

---

## 🚀 Ví Dụ: Thêm Sản Phẩm Vào Giỏ Hàng

### Cách Mới (Firebase)

```java
// Lưu cart item vào Firestore
FirebaseHelper.getFirestore()
    .collection("cart")
    .document(userId)
    .collection("items")
    .add(new CartItem(productId, quantity, size))
    .addOnSuccessListener(documentReference -> {
        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
    })
    .addOnFailureListener(e -> {
        Toast.makeText(context, "Error: " + e.getMessage(), 
                      Toast.LENGTH_SHORT).show();
    });
```

### Lấy Giỏ Hàng Từ Firestore

```java
// Real-time listener (tự động update khi có thay đổi)
FirebaseHelper.getFirestore()
    .collection("cart")
    .document(userId)
    .collection("items")
    .addSnapshotListener((querySnapshot, e) -> {
        if (e != null) {
            Log.w("Firestore", "Listen failed", e);
            return;
        }

        List<CartItem> cartItems = new ArrayList<>();
        for (QueryDocumentSnapshot doc : querySnapshot) {
            CartItem item = doc.toObject(CartItem.class);
            cartItems.add(item);
        }
        
        // Update UI
        adapter.setItems(cartItems);
    });
```

---

## 🎯 So Sánh Code

### Lấy Products (SQLite vs Firebase)

**SQLite (Cũ):**
```java
// DatabaseHelper.java
Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, null, null, null);
List<Product> products = new ArrayList<>();
while (cursor.moveToNext()) {
    Product p = new Product(...);
    products.add(p);
}
cursor.close();
```

**Firebase (Mới):**
```java
// FirestoreService.java (tạo mới)
FirebaseHelper.getFirestore()
    .collection("products")
    .get()
    .addOnSuccessListener(queryDocumentSnapshot -> {
        List<Product> products = queryDocumentSnapshot
            .toObjects(Product.class);
    });
```

---

## ⚠️ Một Số Lưu Ý

### Cost
```
Firestore Pricing:
- 50,000 free reads/day
- 20,000 free writes/day
- Storage: 1GB free

Ước tính: ~$1-5/tháng cho app vừa và nhỏ
```

### Offline Data
```
Firebase có Offline Persistence:
- Tự động lưu dữ liệu local
- Khi online trở lại, tự động sync
- Cần enable: FirebaseFirestore.getInstance().enablePersistence()
```

### Authentication
```
Firebase Auth bao gồm:
✓ Email/Password
✓ Google
✓ Facebook
✓ GitHub
✓ Apple
✓ Phone Number
✓ Custom (JWT)
```

---

## 📋 Checklist Chuyển Sang Firebase

- [ ] Tạo Firebase Project trên console.firebase.google.com
- [ ] Thêm Android app vào Firebase project
- [ ] Download google-services.json vào app/
- [ ] Thêm Firebase dependencies vào build.gradle.kts
- [ ] Enable Authentication (Email/Password, Google)
- [ ] Enable Firestore Database
- [ ] Enable Cloud Storage
- [ ] Tạo FirebaseHelper.java class
- [ ] Viết FirebaseAuthService
- [ ] Viết FirestoreService cho products
- [ ] Viết FirestoreService cho cart
- [ ] Viết FirestoreService cho wishlist
- [ ] Upload ảnh lên Cloud Storage
- [ ] Test login/register
- [ ] Test cart operations
- [ ] Deploy app

---

## 🎉 Kết Quả Cuối Cùng

Sau khi chuyển Firebase:
```
✅ Giỏ hàng được lưu vĩnh viễn
✅ Yêu thích được lưu vĩnh viễn
✅ Sync tự động trên nhiều thiết bị
✅ Offline vẫn dùng được
✅ Không cần quản lý server
✅ Tự động backup
✅ Secure & Scalable
✅ Phí rẻ (hoặc free cho app nhỏ)
```

# Cap nhat 2026-06-09

Ban hien tai cua app chua ket noi Firebase that. Source co Firebase dependencies de san sang migration, nhung chua co `app/google-services.json` va chua apply plugin `com.google.gms.google-services`. App dang chay on-device bang SQLite local. Muon chuyen sang Firebase can tao Firebase project, tai `google-services.json`, bat Authentication/Firestore/Storage, sau do moi thay AuthManager/DatabaseHelper bang service Firebase.
