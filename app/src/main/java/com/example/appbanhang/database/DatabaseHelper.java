package com.example.appbanhang.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.appbanhang.models.Product;
import com.example.appbanhang.models.CartItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    // Database info
    private static final String DATABASE_NAME = "smarteshop.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    private static final String TABLE_PRODUCTS = "products";
    private static final String TABLE_USERS = "users";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_FAVORITES = "favorites";

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
        // Tạo bảng Products
        String CREATE_PRODUCTS_TABLE = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
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

        // Tạo bảng Users
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY," +
                COLUMN_USER_NAME + " TEXT," +
                COLUMN_USER_EMAIL + " TEXT UNIQUE," +
                COLUMN_USER_PHONE + " TEXT," +
                "password TEXT" +
                ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Tạo bảng Orders
        String CREATE_ORDERS_TABLE = "CREATE TABLE " + TABLE_ORDERS + " (" +
                "order_id INTEGER PRIMARY KEY," +
                "user_id INTEGER," +
                "total_amount REAL," +
                "order_status TEXT," +
                "payment_method TEXT," +
                "order_date TEXT" +
                ")";
        db.execSQL(CREATE_ORDERS_TABLE);

        // Tạo bảng Favorites
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + " (" +
                "id INTEGER PRIMARY KEY," +
                "user_id INTEGER," +
                "product_id INTEGER," +
                "UNIQUE(user_id, product_id)" +
                ")";
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    // ========== PRODUCT OPERATIONS ==========

    public void addProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCT_NAME, product.getName());
        values.put(COLUMN_PRODUCT_CATEGORY, product.getCategory());
        values.put(COLUMN_PRODUCT_PRICE, product.getPrice());
        values.put(COLUMN_PRODUCT_IMAGE, product.getImageUrl());
        values.put(COLUMN_PRODUCT_DESCRIPTION, product.getDescription());
        values.put(COLUMN_PRODUCT_RATING, product.getRating());
        values.put(COLUMN_PRODUCT_BRAND, product.getBrand());
        
        db.insert(TABLE_PRODUCTS, null, values);
        db.close();
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
        
        db.insert(TABLE_FAVORITES, null, values);
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

    // ========== USER OPERATIONS (Auth) ==========

    public boolean addUser(String fullName, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, fullName);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PHONE, phone);
        values.put("password", password);
        
        try {
            db.insert(TABLE_USERS, null, values);
            db.close();
            return true;
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

