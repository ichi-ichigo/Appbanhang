package com.example.appbanhang.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.appbanhang.models.CartItem;
import com.example.appbanhang.models.Order;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    // Database info
    private static final String DATABASE_NAME = "smarteshop.db";
    private static final int DATABASE_VERSION = 3;

    // Table names
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_CART_ITEMS = "cart_items";
    private static final String TABLE_BANNERS = "banners";
    private static final String TABLE_BRANDS = "brands";

    // Product columns
    private static final String COLUMN_PRODUCT_ID = "id";
    private static final String COLUMN_PRODUCT_NAME = "name";
    private static final String COLUMN_PRODUCT_CATEGORY = "category";
    private static final String COLUMN_PRODUCT_PRICE = "price";
    private static final String COLUMN_PRODUCT_IMAGE = "image_url";
    private static final String COLUMN_PRODUCT_DESCRIPTION = "description";
    private static final String COLUMN_PRODUCT_RATING = "rating";
    private static final String COLUMN_PRODUCT_BRAND = "brand";

    // User columns
    private static final String COLUMN_USER_ID = "id";
    private static final String COLUMN_USER_NAME = "full_name";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PHONE = "phone_number";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ensureSchema(db);
    }

    private void ensureSchema(SQLiteDatabase db) {
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS + " (" +
                COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY," +
                COLUMN_PRODUCT_NAME + " TEXT," +
                COLUMN_PRODUCT_CATEGORY + " TEXT," +
                COLUMN_PRODUCT_PRICE + " REAL," +
                COLUMN_PRODUCT_IMAGE + " TEXT," +
                COLUMN_PRODUCT_DESCRIPTION + " TEXT," +
                COLUMN_PRODUCT_RATING + " REAL," +
                COLUMN_PRODUCT_BRAND + " TEXT" +
                ")";
        db.execSQL(CREATE_PRODUCTS_TABLE);
        ensureColumn(db, TABLE_PRODUCTS, "thumbnail_url", "TEXT");
        ensureColumn(db, TABLE_PRODUCTS, "image_urls", "TEXT");
        ensureColumn(db, TABLE_PRODUCTS, "discount", "REAL DEFAULT 0");
        ensureColumn(db, TABLE_PRODUCTS, "promotion", "TEXT");
        ensureColumn(db, TABLE_PRODUCTS, "stock", "INTEGER DEFAULT 0");
        ensureColumn(db, TABLE_PRODUCTS, "color", "TEXT");
        ensureColumn(db, TABLE_PRODUCTS, "is_new", "INTEGER DEFAULT 0");

        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY," +
                COLUMN_USER_NAME + " TEXT," +
                COLUMN_USER_EMAIL + " TEXT UNIQUE," +
                COLUMN_USER_PHONE + " TEXT," +
                "password TEXT" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);
        ensureColumn(db, TABLE_USERS, COLUMN_USER_NAME, "TEXT");
        ensureColumn(db, TABLE_USERS, COLUMN_USER_EMAIL, "TEXT");
        ensureColumn(db, TABLE_USERS, COLUMN_USER_PHONE, "TEXT");
        ensureColumn(db, TABLE_USERS, "password", "TEXT");

        String CREATE_ORDERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ORDERS + " (" +
                "order_id INTEGER PRIMARY KEY," +
                "user_id INTEGER," +
                "total_amount REAL," +
                "order_status TEXT," +
                "payment_method TEXT," +
                "order_date TEXT" +
                ")";
        db.execSQL(CREATE_ORDERS_TABLE);
        ensureColumn(db, TABLE_ORDERS, "delivery_address", "TEXT");
        ensureColumn(db, TABLE_ORDERS, "promo_code", "TEXT");
        ensureColumn(db, TABLE_ORDERS, "discount", "REAL DEFAULT 0");

        String CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + " (" +
                "id INTEGER PRIMARY KEY," +
                "user_id INTEGER," +
                "product_id INTEGER," +
                "UNIQUE(user_id, product_id)" +
                ")";
        db.execSQL(CREATE_FAVORITES_TABLE);

        String CREATE_CART_ITEMS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_CART_ITEMS + " (" +
                "id INTEGER PRIMARY KEY," +
                "user_id INTEGER," +
                "product_id INTEGER," +
                "name TEXT," +
                "category TEXT," +
                "price REAL," +
                "image_url TEXT," +
                "description TEXT," +
                "rating REAL," +
                "brand TEXT," +
                "quantity INTEGER," +
                "selected_size TEXT," +
                "UNIQUE(user_id, product_id, selected_size)" +
                ")";
        db.execSQL(CREATE_CART_ITEMS_TABLE);

        String CREATE_BANNERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BANNERS + " (" +
                "id INTEGER PRIMARY KEY," +
                "title TEXT," +
                "image_url TEXT," +
                "action_url TEXT," +
                "display_order INTEGER," +
                "type TEXT," +
                "is_active INTEGER," +
                "background_color TEXT," +
                "subtitle TEXT" +
                ")";
        db.execSQL(CREATE_BANNERS_TABLE);

        String CREATE_BRANDS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BRANDS + " (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "logo_url TEXT," +
                "cover_image_url TEXT," +
                "description TEXT" +
                ")";
        db.execSQL(CREATE_BRANDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ensureSchema(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensureSchema(db);
    }

    private void ensureColumn(SQLiteDatabase db, String tableName, String columnName, String columnType) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        boolean exists = false;
        if (cursor.moveToFirst()) {
            do {
                if (columnName.equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                    exists = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (!exists) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        }
    }

    private String getOptionalString(Cursor cursor, String columnName) {
        int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex < 0 || cursor.isNull(columnIndex)) {
            return "";
        }
        return cursor.getString(columnIndex);
    }

    // ========== PRODUCT OPERATIONS ==========

    public void addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_ID, product.getId());
        values.put(COLUMN_PRODUCT_NAME, product.getName());
        values.put(COLUMN_PRODUCT_CATEGORY, product.getCategory());
        values.put(COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(COLUMN_PRODUCT_IMAGE, product.getImageUrl());
        values.put(COLUMN_PRODUCT_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRODUCT_RATING, product.getRating());
        values.put(COLUMN_PRODUCT_BRAND, product.getBrand());
        values.put("thumbnail_url", product.getThumbnailUrl());
        values.put("image_urls", joinImageUrls(product.getImageUrls()));
        values.put("discount", product.getDiscount());
        values.put("promotion", product.getPromotion());
        values.put("stock", product.getStock());
        values.put("color", product.getColor());
        values.put("is_new", product.isNew() ? 1 : 0);
        
        db.insertWithOnConflict(TABLE_PRODUCTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void seedProductsIfEmpty(List<Product> products) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PRODUCTS, new String[]{"COUNT(*)"}, null, null, null, null, null);
        boolean hasProducts = false;
        if (cursor.moveToFirst()) {
            hasProducts = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();

        if (hasProducts) {
            return;
        }

        for (Product product : products) {
            addProduct(product);
        }
    }

    private String joinImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String url : imageUrls) {
            if (builder.length() > 0) {
                builder.append("|");
            }
            builder.append(url);
        }
        return builder.toString();
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null, 
                                 null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getDouble(6),
                    cursor.getString(7)
                );
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        
        return products;
    }

    public Product getProduct(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_PRODUCTS, null, 
                                 COLUMN_PRODUCT_ID + "=?", 
                                 new String[]{String.valueOf(id)},
                                 null, null, null);
        
        Product product = null;
        if (cursor.moveToFirst()) {
            product = new Product(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getDouble(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getDouble(6),
                cursor.getString(7)
            );
        }
        cursor.close();
        db.close();
        
        return product;
    }

    // ========== FAVORITE OPERATIONS ==========

    public void addToFavorites(int userId, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", productId);
        
        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void removeFromFavorites(int userId, int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, "user_id=? AND product_id=?",
                  new String[]{String.valueOf(userId), String.valueOf(productId)});
        db.close();
    }

    public List<Product> getFavoriteProducts(int userId) {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT p.* FROM " + TABLE_PRODUCTS + " p " +
                      "INNER JOIN " + TABLE_FAVORITES + " f " +
                      "ON p.id = f.product_id WHERE f.user_id=?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getDouble(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getDouble(6),
                    cursor.getString(7)
                );
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        
        return products;
    }

    public List<Integer> getFavoriteProductIds(int userId) {
        List<Integer> productIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{"product_id"}, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                productIds.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return productIds;
    }

    public long addOrder(int userId, double totalAmount, String paymentMethod, String orderStatus) {
        return addOrder(userId, totalAmount, paymentMethod, orderStatus, "");
    }

    public long addOrder(int userId, double totalAmount, String paymentMethod, String orderStatus, String deliveryAddress) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("total_amount", totalAmount);
        values.put("order_status", orderStatus);
        values.put("payment_method", paymentMethod);
        values.put("order_date", String.valueOf(System.currentTimeMillis()));
        values.put("delivery_address", deliveryAddress);

        long orderId = db.insert(TABLE_ORDERS, null, values);
        db.close();
        return orderId;
    }

    public List<Order> getOrders(int userId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, "order_id DESC");

        if (cursor.moveToFirst()) {
            do {
                Order order = new Order(userId);
                order.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow("order_id")));
                order.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount")));
                order.setOrderStatus(cursor.getString(cursor.getColumnIndexOrThrow("order_status")));
                order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow("payment_method")));
                order.setDeliveryAddress(getOptionalString(cursor, "delivery_address"));
                String orderDate = cursor.getString(cursor.getColumnIndexOrThrow("order_date"));
                try {
                    order.setOrderDate(new Date(Long.parseLong(orderDate)));
                } catch (Exception ignored) {
                    order.setOrderDate(new Date());
                }
                orders.add(order);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return orders;
    }

    public int getOrderCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ORDERS, new String[]{"COUNT(*)"}, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, null);

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    public void saveCartItem(int userId, CartItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        Product product = item.getProduct();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("product_id", product.getId());
        values.put("name", product.getName());
        values.put("category", product.getCategory());
        values.put("price", product.getPrice());
        values.put("image_url", product.getImageUrl());
        values.put("description", product.getDescription());
        values.put("rating", product.getRating());
        values.put("brand", product.getBrand());
        values.put("quantity", item.getQuantity());
        values.put("selected_size", item.getSelectedSize());

        db.insertWithOnConflict(TABLE_CART_ITEMS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CART_ITEMS, null, "user_id=?",
                new String[]{String.valueOf(userId)}, null, null, "id ASC");

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow("product_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_url")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("rating")),
                        cursor.getString(cursor.getColumnIndexOrThrow("brand"))
                );
                CartItem item = new CartItem(
                        product,
                        cursor.getInt(cursor.getColumnIndexOrThrow("quantity")),
                        cursor.getString(cursor.getColumnIndexOrThrow("selected_size"))
                );
                item.setCartItemId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                items.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return items;
    }

    public void removeCartItem(int userId, int productId, String selectedSize) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART_ITEMS, "user_id=? AND product_id=? AND selected_size=?",
                new String[]{String.valueOf(userId), String.valueOf(productId), selectedSize});
        db.close();
    }

    public void clearCartItems(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CART_ITEMS, "user_id=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    // ========== USER OPERATIONS (Auth) ==========

    public boolean addUser(String fullName, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, fullName);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PHONE, phone);
        values.put("password", password);
        
        try {
            long rowId = db.insert(TABLE_USERS, null, values);
            db.close();
            return rowId != -1;
        } catch (Exception e) {
            db.close();
            return false; // Email đã tồn tại hoặc lỗi khác
        }
    }

    public boolean getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USER_EMAIL + "=?", 
                                 new String[]{email}, null, null, null);
        
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        
        return exists;
    }

    public Cursor getUserCredentials(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COLUMN_USER_EMAIL + "=?",
                       new String[]{email}, null, null, null);
    }

    public User getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_USER_EMAIL + "=?",
                new String[]{email}, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE))
            );
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
        }

        cursor.close();
        db.close();
        return user;
    }

    public boolean updateUserProfile(int userId, String fullName, String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, fullName);
        values.put(COLUMN_USER_PHONE, phoneNumber);
        int rows = db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        db.close();
        return rows > 0;
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        int rows = db.update(TABLE_USERS, values, COLUMN_USER_EMAIL + "=?",
                new String[]{email});
        db.close();
        return rows > 0;
    }

    public void addUserCredential(String email, String password) {
        // In production, this should be encrypted and stored securely
        // For now, we'll store it in the users table as a separate column
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Update user with password (simplified - should be encrypted in production)
        ContentValues values = new ContentValues();
        values.put("password", password);
        db.update(TABLE_USERS, values, COLUMN_USER_EMAIL + "=?", new String[]{email});
        db.close();
    }

    public boolean verifyUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"password"}, 
                                 COLUMN_USER_EMAIL + "=?",
                                 new String[]{email}, null, null, null);
        
        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(0);
            cursor.close();
            db.close();
            return storedPassword != null && storedPassword.equals(password);
        }
        cursor.close();
        db.close();
        return false;
    }
}

