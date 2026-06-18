package com.example.appbanhang.firebase;

import com.example.appbanhang.models.Banner;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.models.Voucher;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String errorMessage);
    }

    public interface VouchersCallback {
        void onSuccess(List<Voucher> vouchers);
        void onError(String errorMessage);
    }

    public interface OperationCallback {
        void onSuccess();
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
                            normalizeProductImage(product);
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
                    if (product != null) {
                        normalizeProductImage(product);
                        products.add(product);
                    }
                }
                callback.onSuccess(products);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    /**
     * Tìm kiếm sản phẩm theo tên (contains — dùng prefix match)
     */
    public void searchProducts(String keyword, ProductsCallback callback) {
        String keywordLower = keyword.toLowerCase(Locale.ROOT).trim();
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
                    if (product != null) {
                        normalizeProductImage(product);
                        products.add(product);
                    }
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
                    if (product != null) {
                        normalizeProductImage(product);
                        products.add(product);
                    }
                }
                callback.onSuccess(products);
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void fetchSingleProductById(int productId, ProductCallback callback) {
        fetchProductById(productId, new ProductsCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products.isEmpty()) {
                    callback.onError("Không tìm thấy sản phẩm trên Firebase");
                    return;
                }
                callback.onSuccess(products.get(0));
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void addProduct(Product product, OperationCallback callback) {
        normalizeProductBeforeSave(product);
        FirebaseHelper.getFirestore().collection("products")
                .document(String.valueOf(product.getId()))
                .set(product)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError("Lỗi thêm sản phẩm: " + e.getMessage()));
    }

    public void updateProduct(Product product, OperationCallback callback) {
        normalizeProductBeforeSave(product);
        FirebaseHelper.getFirestore().collection("products")
                .whereEqualTo("id", product.getId())
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        addProduct(product, callback);
                        return;
                    }
                    String documentId = querySnapshot.getDocuments().get(0).getId();
                    FirebaseHelper.getFirestore().collection("products")
                            .document(documentId)
                            .set(product, SetOptions.merge())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(e -> callback.onError("Lỗi cập nhật sản phẩm: " + e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError("Lỗi tìm sản phẩm: " + e.getMessage()));
    }

    public void deleteProduct(int productId, OperationCallback callback) {
        FirebaseHelper.getFirestore().collection("products")
                .whereEqualTo("id", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        callback.onSuccess();
                        return;
                    }

                    final int[] remaining = {querySnapshot.size()};
                    final boolean[] failed = {false};
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        doc.getReference().delete()
                                .addOnSuccessListener(unused -> {
                                    remaining[0]--;
                                    if (remaining[0] == 0 && !failed[0]) {
                                        callback.onSuccess();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!failed[0]) {
                                        failed[0] = true;
                                        callback.onError("Lỗi xóa sản phẩm: " + e.getMessage());
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> callback.onError("Lỗi tìm sản phẩm: " + e.getMessage()));
    }

    public void seedProductsToFirebase(List<Product> products, OperationCallback callback) {
        if (products == null || products.isEmpty()) {
            callback.onSuccess();
            return;
        }

        final int[] remaining = {products.size()};
        final boolean[] failed = {false};
        for (Product product : products) {
            normalizeProductBeforeSave(product);
            FirebaseHelper.getFirestore().collection("products")
                    .document(String.valueOf(product.getId()))
                    .set(product, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        remaining[0]--;
                        if (remaining[0] == 0 && !failed[0]) {
                            callback.onSuccess();
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!failed[0]) {
                            failed[0] = true;
                            callback.onError("Lỗi chuyển dữ liệu lên Firebase: " + e.getMessage());
                        }
                    });
        }
    }

    private void normalizeProductImage(Product product) {
        if (isBlank(product.getImageUrl()) && !isBlank(product.getThumbnailUrl())) {
            product.setImageUrl(product.getThumbnailUrl());
        }
        if (isBlank(product.getImageUrl())
                && product.getImageUrls() != null
                && !product.getImageUrls().isEmpty()) {
            product.setImageUrl(product.getImageUrls().get(0));
        }
        if (product.getImageUrls() == null) {
            product.setImageUrls(new ArrayList<>());
        }
    }

    private void normalizeProductBeforeSave(Product product) {
        if (product.getImageUrls() == null) {
            product.setImageUrls(new ArrayList<>());
        }
        if (!isBlank(product.getImageUrl()) && product.getImageUrls().isEmpty()) {
            product.getImageUrls().add(product.getImageUrl());
        }
        if (isBlank(product.getThumbnailUrl())) {
            product.setThumbnailUrl(product.getImageUrl());
        }
        normalizeProductImage(product);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    // ==================== FETCH BANNERS ====================

    /**
     * Lấy toàn bộ banner đang active từ Firestore collection "banners"
     */
    public void fetchBanners(BannersCallback callback) {
        FirebaseHelper.getFirestore().collection("banners")
            .whereEqualTo("isActive", true)
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
                banners.sort((first, second) ->
                        Integer.compare(first.getDisplayOrder(), second.getDisplayOrder()));
                callback.onSuccess(banners);
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi tải banner: " + e.getMessage());
            });
    }
    public void fetchVouchers(VouchersCallback callback) {
        FirebaseHelper.getFirestore().collection("vouchers")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Voucher> vouchers = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        if ("_meta".equals(doc.getId())) {
                            continue;
                        }
                        try {
                            Voucher voucher = doc.toObject(Voucher.class);
                            if (voucher != null) {
                                voucher.setDocId(doc.getId());
                                if (isBlank(voucher.getCode())) {
                                    voucher.setCode(doc.getId());
                                }
                                vouchers.add(voucher);
                            }
                        } catch (Exception ignored) {
                            // Bỏ qua voucher lỗi để vẫn dùng được các voucher còn lại.
                        }
                    }
                    vouchers.sort((first, second) -> {
                        String firstCode = first.getCode() == null ? "" : first.getCode();
                        String secondCode = second.getCode() == null ? "" : second.getCode();
                        return firstCode.compareToIgnoreCase(secondCode);
                    });
                    callback.onSuccess(vouchers);
                })
                .addOnFailureListener(e ->
                        callback.onError("Lỗi tải voucher: " + e.getMessage()));
    }
}
