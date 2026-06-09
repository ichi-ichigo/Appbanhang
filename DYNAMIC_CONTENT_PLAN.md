# 📸 Kế Hoạch Nâng Cấp: Thêm Hình Ảnh, Banner & Tính Năng Động

## 🎯 Mục Tiêu Tổng Quát
Biến ứng dụng từ khung cơ bản thành ứng dụng **sinh động, hấp dẫn** với:
- ✅ Hình ảnh sản phẩm chất lượng cao
- ✅ Banner quảng cáo động
- ✅ Hiệu ứng chuyển động (animation)
- ✅ Carousel ảnh lớn trên trang chủ
- ✅ Dữ liệu thực tế cho sản phẩm

---

## 📊 PHẦN 1: CẤU TRÚC DỮ LIỆU

### 1.1 Mở Rộng Model Product

**File:** `models/Product.java`

```java
public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private String imageUrl;                    // ✅ Đã có
    private String description;
    private double rating;
    private int reviewCount;
    private String brand;
    private boolean isFavorite;
    
    // ⭐ THÊM MỚI:
    private List<String> imageUrls;            // Danh sách 3-5 ảnh sản phẩm
    private String thumbnailUrl;               // Ảnh nhỏ cho grid
    private double discount;                   // % giảm giá
    private String promotion;                  // "NEW", "HOT DEAL", "TRENDING"
    private int stock;                         // Số lượng còn
    private String color;                      // Màu sắc chính
    private boolean isNew;                     // Sản phẩm mới
    
    // Constructor + Getters/Setters
}
```

### 1.2 Tạo Model Banner Mới

**File:** `models/Banner.java`

```java
public class Banner {
    private int id;
    private String title;                      // "Summer Collection", "50% OFF"
    private String imageUrl;                   // Ảnh banner lớn
    private String actionUrl;                  // Link sản phẩm khi click
    private int displayOrder;                  // Thứ tự hiển thị
    private String type;                       // "PROMO", "BRAND", "SEASONAL"
    private boolean isActive;
    private String backgroundColor;            // Màu overlay
    
    // Getters/Setters
}
```

### 1.3 Model Brand

**File:** `models/Brand.java`

```java
public class Brand {
    private int id;
    private String name;                       // "Nike", "Adidas"
    private String logoUrl;                    // Logo thương hiệu
    private String coverImageUrl;              // Ảnh nền
    private String description;
    
    // Getters/Setters
}
```

---

## 🖼️ PHẦN 2: QUẢN LÝ HÌNH ẢNH

### 2.1 Tạo Image Loading Manager

**File:** `managers/ImageManager.java`

```java
public class ImageManager {
    private static ImageManager instance;
    
    private ImageManager() {}
    
    public static ImageManager getInstance() {
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }
    
    // Load ảnh từ URL vào ImageView
    public void loadImage(String imageUrl, ImageView imageView) {
        // Dùng Glide hoặc Picasso
        Glide.with(imageView.getContext())
             .load(imageUrl)
             .placeholder(R.drawable.placeholder_image)
             .error(R.drawable.error_image)
             .into(imageView);
    }
    
    // Load ảnh với animation
    public void loadImageWithAnimation(String imageUrl, ImageView imageView) {
        Glide.with(imageView.getContext())
             .load(imageUrl)
             .transition(DrawableTransitionOptions.withCrossFade())
             .into(imageView);
    }
    
    // Load ảnh lớn từ danh sách
    public void loadProductGallery(List<String> imageUrls, ImageView mainImage, 
                                   RecyclerView thumbnailView) {
        // Logic load gallery
    }
}
```

### 2.2 Thêm Dependencies vào build.gradle.kts

```gradle
dependencies {
    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    
    // Carousel/ViewPager
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    
    // Animations
    implementation("com.airbnb.android:lottie:6.0.0")
}
```

---

## 🎨 PHẦN 3: BANNER & CAROUSEL

### 3.1 Layout Carousel Banner

**File:** `res/layout/carousel_banner_layout.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:background="#F5F5F5">
    
    <!-- ViewPager2 cho carousel -->
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/bannerViewPager"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
    <!-- Dot indicator -->
    <LinearLayout
        android:id="@+id/dotsIndicator"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_alignBottom="@id/bannerViewPager"
        android:layout_centerHorizontal="true"
        android:layout_marginBottom="12dp"
        android:gravity="center"
        android:orientation="horizontal" />
    
</RelativeLayout>
```

### 3.2 Item Banner Layout

**File:** `res/layout/item_banner.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="200dp">
    <!-- Banner Image -->
    <ImageView
        android:id="@+id/bannerImage"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop" />
    
    <!-- Gradient overlay -->
    <View
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="@drawable/gradient_overlay" />
    
    <!-- Text Content -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center_vertical"
        android:paddingStart="24dp"
        android:paddingEnd="24dp"
        android:orientation="vertical">
        
        <TextView
            android:id="@+id/bannerTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="24sp"
            android:textStyle="bold"
            android:textColor="@color/white" />
        
        <TextView
            android:id="@+id/bannerSubtitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:textColor="@color/white"
            android:layout_marginTop="8dp" />
        
    </LinearLayout>
    
</FrameLayout>
```

### 3.3 Adapter cho Banner

**File:** `adapters/BannerAdapter.java`

```java
public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    
    private List<Banner> bannerList;
    private Context context;
    private OnBannerClickListener listener;
    
    public BannerAdapter(List<Banner> bannerList, Context context) {
        this.bannerList = bannerList;
        this.context = context;
    }
    
    @Override
    public BannerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(BannerViewHolder holder, int position) {
        Banner banner = bannerList.get(position);
        
        // Load ảnh banner
        ImageManager.getInstance()
                .loadImage(banner.getImageUrl(), holder.bannerImage);
        
        holder.bannerTitle.setText(banner.getTitle());
        holder.bannerSubtitle.setText(banner.getSubtitle());
        
        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBannerClick(banner);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return bannerList.size();
    }
    
    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        TextView bannerTitle, bannerSubtitle;
        
        BannerViewHolder(View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.bannerImage);
            bannerTitle = itemView.findViewById(R.id.bannerTitle);
            bannerSubtitle = itemView.findViewById(R.id.bannerSubtitle);
        }
    }
    
    public interface OnBannerClickListener {
        void onBannerClick(Banner banner);
    }
    
    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.listener = listener;
    }
}
```

---

## 📱 PHẦN 4: NÂNG CẬP TRANG CHỦ

### 4.1 Layout Trang Chủ Mới

**File:** `res/layout/activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">
    
    <!-- Header -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:padding="16dp"
        android:orientation="horizontal"
        android:gravity="center_vertical">
        
        <TextView
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="Hi, Smarteshop!"
            android:textSize="20sp"
            android:textStyle="bold" />
        
        <ImageView
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:src="@drawable/ic_bell"
            android:contentDescription="Notification" />
    </LinearLayout>
    
    <!-- ScrollView để scroll toàn bộ content -->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1">
        
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical">
            
            <!-- 🔥 CAROUSEL BANNER (NEW) -->
            <include
                layout="@layout/carousel_banner_layout"
                android:layout_width="match_parent"
                android:layout_height="200dp" />
            
            <!-- Section Danh Mục Thương Hiệu -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:padding="16dp"
                android:orientation="vertical">
                
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Popular Brands"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp" />
                
                <HorizontalScrollView
                    android:layout_width="match_parent"
                    android:layout_height="100dp"
                    android:scrollbars="none">
                    
                    <LinearLayout
                        android:id="@+id/brandContainer"
                        android:layout_width="wrap_content"
                        android:layout_height="match_parent"
                        android:orientation="horizontal" />
                    
                </HorizontalScrollView>
            </LinearLayout>
            
            <!-- Section Sản Phẩm Nổi Bật -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:padding="16dp"
                android:orientation="vertical">
                
                <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Featured Products"
                    android:textSize="16sp"
                    android:textStyle="bold"
                    android:layout_marginBottom="12dp" />
                
                <!-- Grid sản phẩm 2 cột -->
                <androidx.recyclerview.widget.RecyclerView
                    android:id="@+id/recyclerProducts"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:nestedScrollingEnabled="false" />
            </LinearLayout>
        </LinearLayout>
    </ScrollView>
    
    <!-- Bottom Navigation -->
    <LinearLayout
        android:id="@+id/bottomNavigation"
        android:layout_width="match_parent"
        android:layout_height="56dp"
        android:orientation="horizontal"
        android:gravity="center_vertical"
        android:baselineAligned="false">
        
        <Button
            android:id="@+id/btnHome"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:text="Home"
            style="?attr/borderlessButtonStyle" />
        
        <Button
            android:id="@+id/btnSearch"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:text="Search"
            style="?attr/borderlessButtonStyle" />
        
        <Button
            android:id="@+id/btnCart"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:text="Cart"
            style="?attr/borderlessButtonStyle" />
        
        <Button
            android:id="@+id/btnWishlist"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:text="Wishlist"
            style="?attr/borderlessButtonStyle" />
        
        <Button
            android:id="@+id/btnAccount"
            android:layout_width="0dp"
            android:layout_height="match_parent"
            android:layout_weight="1"
            android:text="Account"
            style="?attr/borderlessButtonStyle" />
    </LinearLayout>
</LinearLayout>
```

### 4.2 Item Sản Phẩm Với Ảnh

**File:** `res/layout/item_product.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:background="@color/white"
    android:elevation="2dp"
    android:layout_margin="8dp">
    
    <!-- Container ảnh + badge -->
    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="180dp">
        
        <!-- Ảnh sản phẩm -->
        <ImageView
            android:id="@+id/productImage"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop"
            android:contentDescription="Product Image" />
        
        <!-- Badge (NEW, HOT, DISCOUNT) -->
        <TextView
            android:id="@+id/badgeLabel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="top|end"
            android:layout_margin="8dp"
            android:paddingStart="8dp"
            android:paddingEnd="8dp"
            android:paddingTop="4dp"
            android:paddingBottom="4dp"
            android:text="NEW"
            android:textColor="@color/white"
            android:textSize="12sp"
            android:textStyle="bold"
            android:background="#FF6B6B" />
        
        <!-- Nút yêu thích -->
        <ImageView
            android:id="@+id/favoriteButton"
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_gravity="bottom|end"
            android:layout_margin="8dp"
            android:src="@drawable/ic_favorite_outline"
            android:contentDescription="Favorite"
            android:background="@drawable/circle_white_bg"
            android:scaleType="centerInside"
            android:padding="8dp" />
    </FrameLayout>
    
    <!-- Thông tin sản phẩm -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="12dp">
        
        <!-- Tên sản phẩm -->
        <TextView
            android:id="@+id/productName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="14sp"
            android:textStyle="bold"
            android:textColor="@color/black"
            android:maxLines="2"
            android:ellipsize="end" />
        
        <!-- Brand -->
        <TextView
            android:id="@+id/productBrand"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:textSize="12sp"
            android:textColor="@color/gray"
            android:layout_marginTop="4dp" />
        
        <!-- Rating -->
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="4dp">
            
            <ImageView
                android:layout_width="16dp"
                android:layout_height="16dp"
                android:src="@drawable/ic_star"
                android:contentDescription="Star" />
            
            <TextView
                android:id="@+id/productRating"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="12sp"
                android:layout_marginStart="4dp" />
        </LinearLayout>
        
        <!-- Giá tiền -->
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:layout_marginTop="8dp"
            android:gravity="center_vertical">
            
            <TextView
                android:id="@+id/productPrice"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="14sp"
                android:textStyle="bold"
                android:textColor="@color/red" />
            
            <!-- Giá cũ (nếu có giảm giá) -->
            <TextView
                android:id="@+id/productOldPrice"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textSize="12sp"
                android:textColor="@color/gray"
                android:layout_marginStart="8dp"
                android:paintFlags="strikeThruText" />
        </LinearLayout>
    </LinearLayout>
</LinearLayout>
```

---

## ✨ PHẦN 5: ANIMATION & HIỆU ỨNG

### 5.1 Fade In Animation

**File:** `res/anim/fade_in.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="500">
    
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0" />
</set>
```

### 5.2 Slide Up Animation

**File:** `res/anim/slide_up.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="400">
    
    <translate
        android:fromYDelta="100%"
        android:toYDelta="0%" />
    
    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0" />
</set>
```

### 5.3 Scale Animation cho Nút

**File:** `res/anim/scale_click.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="200">
    
    <scale
        android:fromXScale="1.0"
        android:fromYScale="1.0"
        android:toXScale="0.95"
        android:toYScale="0.95"
        android:pivotX="50%"
        android:pivotY="50%" />
</set>
```

---

## 📋 PHẦN 6: DỮ LIỆU MẪU (Mock Data)

### 6.1 Tạo Helper Load Dữ Liệu

**File:** `utils/DataProvider.java`

```java
public class DataProvider {
    
    public static List<Banner> getBanners() {
        List<Banner> banners = new ArrayList<>();
        
        banners.add(new Banner(1, "Summer Collection", 
            "https://via.placeholder.com/500x200/00BCD4/ffffff?text=Summer",
            "summer_collection", 1, "SEASONAL", true));
        
        banners.add(new Banner(2, "50% OFF Nike Shoes", 
            "https://via.placeholder.com/500x200/8E24AA/ffffff?text=50%+OFF",
            "nike_sale", 2, "PROMO", true));
        
        return banners;
    }
    
    public static List<Brand> getBrands() {
        List<Brand> brands = new ArrayList<>();
        
        brands.add(new Brand(1, "Nike", 
            "https://via.placeholder.com/60x60?text=Nike",
            "https://via.placeholder.com/500x200?text=Nike",
            "Nike Official"));
        
        brands.add(new Brand(2, "Adidas", 
            "https://via.placeholder.com/60x60?text=Adidas",
            "https://via.placeholder.com/500x200?text=Adidas",
            "Adidas Official"));
        
        brands.add(new Brand(3, "Puma", 
            "https://via.placeholder.com/60x60?text=Puma",
            "https://via.placeholder.com/500x200?text=Puma",
            "Puma Official"));
        
        return brands;
    }
    
    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();
        
        products.add(new Product(1, "Air Jordan Retro", "Sneakers",
            299.99,
            "https://via.placeholder.com/300x300/FF6B6B/ffffff?text=Air+Jordan",
            "Classic Air Jordan Retro 11",
            4.8, "Nike"));
        
        products.add(new Product(2, "Adidas Ultra Boost", "Running",
            250.00,
            "https://via.placeholder.com/300x300/4ECDC4/ffffff?text=Ultra+Boost",
            "Ultra Boost Running Shoes",
            4.6, "Adidas"));
        
        // ... thêm nhiều sản phẩm hơn
        
        return products;
    }
}
```

---

## 🔧 PHẦN 7: HƯỚNG DẪN THỰC HIỆN

### Giai Đoạn 1: Setup Cơ Bản (1-2 ngày)
```
[x] Thêm Glide dependency
[x] Tạo ImageManager
[x] Tạo models Banner & Brand
[x] Tạo DataProvider với dữ liệu mẫu
```

### Giai Đoạn 2: Xây Dựng Banner (1 ngày)
```
[x] Tạo layout carousel_banner_layout.xml
[x] Tạo layout item_banner.xml
[x] Tạo BannerAdapter
[x] Integrate vào MainActivity
[x] Test carousel & swipe
```

### Giai Đoạn 3: Nâng Cấp Sản Phẩm (1-2 ngày)
```
[x] Cập nhật item_product.xml với badge & yêu thích
[x] Cập nhật ProductAdapter để hiển thị ảnh
[x] Thêm animation cho sản phẩm
[x] Test load ảnh từ URL
```

### Giai Đoạn 4: Chi Tiết Sản Phẩm (1 ngày)
```
[x] Tạo image gallery (ViewPager2) trong ProductDetailActivity
[x] Hiển thị thumbnail list
[x] Add animation khi chuyển ảnh
```

### Giai Đoạn 5: Animation & Polish (1 ngày)
```
[x] Thêm fade in animation
[x] Thêm slide up animation
[x] Thêm scale animation cho nút
[x] Optimize hiệu suất
```

---

## 📚 PHẦN 8: DANH SÁCH TỨC THỜI

### Images Cần Thiết
```
drawable/
├── ic_bell.xml              # Icon chuông thông báo
├── ic_favorite_outline.xml  # Icon tim rỗng
├── ic_favorite_filled.xml   # Icon tim đỏ
├── ic_star.xml              # Icon sao
├── placeholder_image.png    # Ảnh placeholder (300x300)
├── error_image.png          # Ảnh lỗi (300x300)
├── gradient_overlay.xml     # Gradient cho banner
└── circle_white_bg.xml      # Background tròn trắng
```

### Layout Files Cần Tạo
```
layout/
├── carousel_banner_layout.xml
├── item_banner.xml
└── [Update] item_product.xml
└── [Update] activity_main.xml
```

### Adapter Files Cần Tạo
```
adapters/
└── BannerAdapter.java
```

### Model Files Cần Tạo
```
models/
├── [Update] Product.java
├── Banner.java
└── Brand.java
```

### Utility Files Cần Tạo
```
utils/
└── DataProvider.java
managers/
└── ImageManager.java
```

---

## 🎨 PHẦN 9: MẪU DỮ LIỆU CHO DATABASE

Nếu sử dụng SQLite, tạo table:

```sql
-- Banner Table
CREATE TABLE banners (
    id INTEGER PRIMARY KEY,
    title TEXT,
    image_url TEXT,
    action_url TEXT,
    display_order INTEGER,
    type TEXT,
    is_active BOOLEAN
);

-- Brand Table
CREATE TABLE brands (
    id INTEGER PRIMARY KEY,
    name TEXT,
    logo_url TEXT,
    cover_image_url TEXT,
    description TEXT
);

-- Product Table (mở rộng)
ALTER TABLE products ADD COLUMN thumbnail_url TEXT;
ALTER TABLE products ADD COLUMN image_urls TEXT; -- JSON array
ALTER TABLE products ADD COLUMN discount REAL;
ALTER TABLE products ADD COLUMN promotion TEXT;
ALTER TABLE products ADD COLUMN stock INTEGER;
ALTER TABLE products ADD COLUMN color TEXT;
ALTER TABLE products ADD COLUMN is_new BOOLEAN;
```

---

## 📲 PHẦN 10: CHECKLIST CUỐI CÙNG

- [ ] Load ảnh không bị lag/crash
- [ ] Banner tự động scroll sau 5 giây
- [ ] Ảnh sản phẩm hiển thị đẹp trên grid
- [ ] Animation mượt mà, không giật
- [ ] Favorite button hoạt động đúng
- [ ] Badge "NEW" hiển thị chính xác
- [ ] Caching ảnh để tiết kiệm data
- [ ] Handle error khi ảnh load thất bại
- [ ] Test trên nhiều kích thước màn hình
- [ ] Performance optimization (memory leak check)

---

## 🚀 KỲ VỌNG KẾT QUẢ

✅ App trở nên **sinh động** với ảnh sản phẩm chất lượng cao  
✅ Banner quảng cáo **tự động cuộn** hấp dẫn khách hàng  
✅ Badge "NEW", "HOT DEAL" giúp **nhấn mạnh sản phẩm**  
✅ Animation **mượt mà** tăng trải nghiệm người dùng  
✅ Danh sách brand **dễ dàng cuộn** và chọn lọc  
✅ Toàn bộ app giờ có **ngoại hình chuyên nghiệp**

---

## 💡 TIPS THÊM

1. **Placeholder Images**: Dùng ảnh placeholderока khi load để UX tốt hơn
2. **Lazy Loading**: Chỉ load ảnh khi cần để tiết kiệm data
3. **Image Caching**: Glide tự động cache, nhưng có thể configure
4. **WebP Format**: Dùng WebP thay PNG/JPG để giảm kích thước
5. **CDN**: Upload ảnh lên Firebase Storage hoặc AWS S3

