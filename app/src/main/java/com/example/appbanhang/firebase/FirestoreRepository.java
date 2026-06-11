package com.example.appbanhang.firebase;

import com.example.appbanhang.models.Banner;
import com.example.appbanhang.models.Product;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class FirestoreRepository {

    private static FirestoreRepository instance;

    private FirestoreRepository() {}

    public static FirestoreRepository getInstance() {
        if (instance == null) {
            instance = new FirestoreRepository();
        }
        return instance;
    }

    // ==================== CALLBACK INTERFACES ====================

    public interface ProductsCallback {
        void onSuccess(List<Product> products);
        void onError(String errorMessage);
    }

    public interface BannersCallback {
        void onSuccess(List<Banner> banners);
        void onError(String errorMessage);
    }

    // ==================== FETCH PRODUCTS ====================

    /**
     * Lấy toàn bộ danh sách sản phẩm từ Firestore collection "products"
     */
    public void fetchProducts(ProductsCallback callback) {
        CollectionReference productsRef = FirebaseHelper.getFirestore().collection("products");

        productsRef
            .orderBy("id", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Product> products = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            products.add(product);
                        }
                    } catch (Exception e) {
                        // Bỏ qua document lỗi, tiếp tục load các document khác
                    }
                }
                callback.onSuccess(products);
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi tải sản phẩm: " + e.getMessage());
            });
    }

    /**
     * Lấy sản phẩm theo brand/thương hiệu
     */
    public void fetchProductsByBrand(String brand, ProductsCallback callback) {
        FirebaseHelper.getFirestore().collection("products")
            .whereEqualTo("brand", brand)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Product> products = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Product product = doc.toObject(Product.class);
                    if (product != null) products.add(product);
                }
                callback.onSuccess(products);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Tìm kiếm sản phẩm theo tên (contains — dùng prefix match)
     */
    public void searchProducts(String keyword, ProductsCallback callback) {
        String keywordLower = keyword.toLowerCase().trim();
        // Firestore không hỗ trợ full-text search, dùng prefix match
        FirebaseHelper.getFirestore().collection("products")
            .orderBy("name")
            .startAt(keyword)
            .endAt(keyword + "\uf8ff")
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Product> products = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Product product = doc.toObject(Product.class);
                    if (product != null) products.add(product);
                }
                callback.onSuccess(products);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Lấy một sản phẩm theo id (field "id" trong document)
     */
    public void fetchProductById(int productId, ProductsCallback callback) {
        FirebaseHelper.getFirestore().collection("products")
            .whereEqualTo("id", productId)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Product> products = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Product product = doc.toObject(Product.class);
                    if (product != null) products.add(product);
                }
                callback.onSuccess(products);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // ==================== FETCH BANNERS ====================

    /**
     * Lấy toàn bộ banner đang active từ Firestore collection "banners"
     */
    public void fetchBanners(BannersCallback callback) {
        FirebaseHelper.getFirestore().collection("banners")
            .whereEqualTo("isActive", true)
            .orderBy("displayOrder", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Banner> banners = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    try {
                        Banner banner = doc.toObject(Banner.class);
                        if (banner != null) {
                            banners.add(banner);
                        }
                    } catch (Exception e) {
                        // Bỏ qua document lỗi
                    }
                }
                callback.onSuccess(banners);
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi tải banner: " + e.getMessage());
            });
    }
}
