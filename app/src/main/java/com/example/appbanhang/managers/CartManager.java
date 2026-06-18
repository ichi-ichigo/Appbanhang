package com.example.appbanhang.managers;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.firebase.FirebaseHelper;
import com.example.appbanhang.models.CartItem;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.models.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private static DatabaseHelper dbHelper;
    private static AuthManager authManager;
    private final List<CartItem> cartItems;
    private double subtotal;
    private double shippingFee = 12000;
    private double discount = 0;
    private String appliedPromoCode = "";

    private CartManager() {
        this.cartItems = new ArrayList<>();
        this.subtotal = 0;
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public static void initialize(DatabaseHelper helper, AuthManager manager) {
        dbHelper = helper;
        authManager = manager;
    }

    public interface CartSyncCallback {
        void onSuccess();
        void onError(String message);
    }

    public void syncFromDatabase() {
        int userId = getCurrentUserId();
        if (dbHelper == null || userId <= 0) {
            cartItems.clear();
            updateSubtotal();
            return;
        }

        replaceCartItems(dbHelper.getCartItems(userId), false);
    }

    public void syncCart() {
        syncCart(null);
    }

    public void syncCart(CartSyncCallback callback) {
        String firebaseUid = getCurrentFirebaseUid();
        if (firebaseUid.isEmpty()) {
            syncFromDatabase();
            if (callback != null) {
                callback.onSuccess();
            }
            return;
        }

        FirebaseHelper.getFirestore()
                .collection("cart")
                .document(firebaseUid)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CartItem> syncedItems = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        CartItem item = toCartItem(document);
                        if (item != null) {
                            syncedItems.add(item);
                        }
                    }

                    if (syncedItems.isEmpty() && !cartItems.isEmpty()) {
                        persistCartCache();
                        persistAllCartItemsToFirestore();
                        updateSubtotal();
                        if (callback != null) {
                            callback.onSuccess();
                        }
                        return;
                    }

                    if (syncedItems.isEmpty()) {
                        List<CartItem> cachedItems = getCartCache();
                        if (!cachedItems.isEmpty()) {
                            replaceCartItems(cachedItems, false);
                            persistAllCartItemsToFirestore();
                            if (callback != null) {
                                callback.onSuccess();
                            }
                            return;
                        }
                    }

                    replaceCartItems(syncedItems, false);
                    persistCartCache();

                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(error -> {
                    syncFromDatabase();
                    if (callback != null) {
                        callback.onError("Khong tai duoc gio hang tu Firebase: " + error.getMessage());
                    }
                });
    }

    public void addToCart(Product product, int quantity, String selectedSize) {
        if (product == null || quantity <= 0) {
            return;
        }

        int currentTotal = getTotalQuantityForProduct(product.getId());
        if (product.getStock() > 0 && currentTotal >= product.getStock()) {
            return;
        }

        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()
                    && item.getSelectedSize().equals(selectedSize)) {
                item.setProduct(product);
                int otherQuantity = currentTotal - item.getQuantity();
                int maxAllowed = product.getStock() > 0
                        ? Math.max(product.getStock() - otherQuantity, 0)
                        : item.getQuantity() + quantity;
                if (maxAllowed <= 0) {
                    return;
                }

                item.setQuantity(Math.min(item.getQuantity() + quantity, maxAllowed));
                updateSubtotal();
                persistCartItem(item);
                return;
            }
        }

        int allowedQuantity = product.getStock() > 0
                ? Math.min(quantity, Math.max(product.getStock() - currentTotal, 0))
                : quantity;
        if (allowedQuantity <= 0) {
            return;
        }

        CartItem newItem = new CartItem(product, allowedQuantity, selectedSize);
        cartItems.add(newItem);
        updateSubtotal();
        persistCartItem(newItem);
    }

    public void addToCart(int productId, String productName, double productPrice, int quantity, String selectedSize) {
        Product product = new Product(productId, productName, "", productPrice, "", "", 5.0, "");
        product.setStock(Integer.MAX_VALUE);
        addToCart(product, quantity, selectedSize);
    }

    public void removeFromCart(CartItem item) {
        cartItems.remove(item);
        int userId = getCurrentUserId();
        if (dbHelper != null && userId > 0) {
            dbHelper.removeCartItem(userId, item.getProduct().getId(), item.getSelectedSize());
        }
        deleteCartItemFromFirestore(item);
        updateSubtotal();
    }

    public void updateQuantity(CartItem item, int newQuantity) {
        if (newQuantity > 0) {
            int maxAllowed = getMaxAllowedQuantity(item);
            item.setQuantity(Math.min(newQuantity, maxAllowed == Integer.MAX_VALUE ? newQuantity : maxAllowed));
            persistCartItem(item);
            updateSubtotal();
        } else {
            removeFromCart(item);
        }
    }

    public void clearCart() {
        int userId = getCurrentUserId();
        if (dbHelper != null && userId > 0) {
            dbHelper.clearCartItems(userId);
        }
        clearCartInFirestore();
        cartItems.clear();
        subtotal = 0;
        discount = 0;
        appliedPromoCode = "";
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public List<CartItem> getAllItems() {
        return cartItems;
    }

    public int getItemCount() {
        return cartItems.size();
    }

    public int getTotalItemsQuantity() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    public int getTotalQuantityForProduct(int productId) {
        int total = 0;
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == productId) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    public int getRemainingStock(Product product) {
        if (product == null || product.getStock() <= 0) {
            return 0;
        }
        return Math.max(product.getStock() - getTotalQuantityForProduct(product.getId()), 0);
    }

    private int getMaxAllowedQuantity(CartItem targetItem) {
        int stock = targetItem.getProduct().getStock();
        if (stock <= 0) {
            return Integer.MAX_VALUE;
        }

        int otherQuantity = 0;
        for (CartItem item : cartItems) {
            if (item == targetItem) {
                continue;
            }
            if (item.getProduct().getId() == targetItem.getProduct().getId()) {
                otherQuantity += item.getQuantity();
            }
        }
        return Math.max(stock - otherQuantity, 0);
    }

    private void updateSubtotal() {
        subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double fee) {
        this.shippingFee = fee;
    }

    public void applyPromoCode(String code, double discountAmount) {
        this.appliedPromoCode = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        this.discount = discountAmount;
    }

    public void clearPromoCode() {
        appliedPromoCode = "";
        discount = 0;
    }

    public String getAppliedPromoCode() {
        return appliedPromoCode;
    }

    public double getDiscount() {
        return discount;
    }

    public double getTotal() {
        return subtotal + shippingFee - discount;
    }

    public double getTotalPrice() {
        return subtotal;
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    private void persistCartItem(CartItem item) {
        int userId = getCurrentUserId();
        if (dbHelper != null && userId > 0) {
            dbHelper.saveCartItem(userId, item);
        }
        saveCartItemToFirestore(item);
    }

    private int getCurrentUserId() {
        if (authManager != null) {
            User currentUser = authManager.getCurrentUser();
            if (currentUser != null && currentUser.getId() > 0) {
                return currentUser.getId();
            }
        }

        String firebaseUid = getCurrentFirebaseUid();
        if (firebaseUid.isEmpty()) {
            return 0;
        }
        return buildStableUserId(firebaseUid);
    }

    private void replaceCartItems(List<CartItem> newItems, boolean persistAdjustedItems) {
        cartItems.clear();
        if (newItems != null) {
            cartItems.addAll(newItems);
        }

        for (CartItem item : new ArrayList<>(cartItems)) {
            int maxAllowed = getMaxAllowedQuantity(item);
            if (maxAllowed <= 0) {
                cartItems.remove(item);
                if (persistAdjustedItems) {
                    deleteCartItemFromFirestore(item);
                }
                continue;
            }

            if (item.getProduct().getStock() > 0 && item.getQuantity() > maxAllowed) {
                item.setQuantity(maxAllowed);
                if (persistAdjustedItems) {
                    persistCartItem(item);
                }
            }
        }

        updateSubtotal();
    }

    private void persistCartCache() {
        int userId = getCurrentUserId();
        if (dbHelper == null || userId <= 0) {
            return;
        }

        dbHelper.clearCartItems(userId);
        for (CartItem item : cartItems) {
            dbHelper.saveCartItem(userId, item);
        }
    }

    private List<CartItem> getCartCache() {
        int userId = getCurrentUserId();
        if (dbHelper == null || userId <= 0) {
            return new ArrayList<>();
        }
        return dbHelper.getCartItems(userId);
    }

    private void persistAllCartItemsToFirestore() {
        for (CartItem item : cartItems) {
            saveCartItemToFirestore(item);
        }
    }

    private void saveCartItemToFirestore(CartItem item) {
        if (item == null || item.getProduct() == null) {
            return;
        }

        String firebaseUid = getCurrentFirebaseUid();
        if (firebaseUid.isEmpty()) {
            return;
        }

        Product product = item.getProduct();
        Map<String, Object> data = new HashMap<>();
        data.put("productId", product.getId());
        data.put("name", product.getName());
        data.put("category", product.getCategory());
        data.put("price", product.getPrice());
        data.put("imageUrl", product.getImageUrl());
        data.put("description", product.getDescription());
        data.put("rating", product.getRating());
        data.put("brand", product.getBrand());
        data.put("stock", Math.max(product.getStock(), item.getQuantity()));
        data.put("imageUrls", product.getImageUrls() == null
                ? new ArrayList<>()
                : new ArrayList<>(product.getImageUrls()));
        data.put("selectedSize", normalizeSize(item.getSelectedSize()));
        data.put("quantity", item.getQuantity());
        data.put("updatedAt", FieldValue.serverTimestamp());

        Map<String, Object> cartMeta = new HashMap<>();
        cartMeta.put("userUid", firebaseUid);
        cartMeta.put("updatedAt", FieldValue.serverTimestamp());

        FirebaseHelper.getFirestore()
                .collection("cart")
                .document(firebaseUid)
                .set(cartMeta, SetOptions.merge());

        FirebaseHelper.getFirestore()
                .collection("cart")
                .document(firebaseUid)
                .collection("items")
                .document(buildCartDocumentId(product.getId(), item.getSelectedSize()))
                .set(data);
    }

    private void deleteCartItemFromFirestore(CartItem item) {
        if (item == null || item.getProduct() == null) {
            return;
        }

        String firebaseUid = getCurrentFirebaseUid();
        if (firebaseUid.isEmpty()) {
            return;
        }

        FirebaseHelper.getFirestore()
                .collection("cart")
                .document(firebaseUid)
                .collection("items")
                .document(buildCartDocumentId(item.getProduct().getId(), item.getSelectedSize()))
                .delete();
    }

    private void clearCartInFirestore() {
        String firebaseUid = getCurrentFirebaseUid();
        if (firebaseUid.isEmpty()) {
            return;
        }

        FirebaseHelper.getFirestore()
                .collection("cart")
                .document(firebaseUid)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        return;
                    }

                    WriteBatch batch = FirebaseHelper.getFirestore().batch();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        batch.delete(document.getReference());
                    }
                    batch.commit();
                });
    }

    private CartItem toCartItem(DocumentSnapshot document) {
        if (document == null || !document.exists()) {
            return null;
        }

        int quantity = Math.max(getInt(document, "quantity"), 1);
        Product product = new Product(
                getInt(document, "productId"),
                getString(document, "name"),
                getString(document, "category"),
                getDouble(document, "price"),
                getString(document, "imageUrl"),
                getString(document, "description"),
                getDouble(document, "rating"),
                getString(document, "brand")
        );
        product.setStock(Math.max(getInt(document, "stock"), quantity));

        List<String> imageUrls = getStringList(document, "imageUrls");
        if (imageUrls.isEmpty() && !product.getImageUrl().isEmpty()) {
            imageUrls.add(product.getImageUrl());
        }
        product.setImageUrls(imageUrls);

        return new CartItem(product, quantity, normalizeSize(getString(document, "selectedSize")));
    }

    private List<String> getStringList(DocumentSnapshot document, String field) {
        List<String> values = new ArrayList<>();
        Object rawValue = document.get(field);
        if (!(rawValue instanceof List<?>)) {
            return values;
        }

        for (Object value : (List<?>) rawValue) {
            if (value != null) {
                values.add(String.valueOf(value));
            }
        }
        return values;
    }

    private String getString(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value.trim();
    }

    private int getInt(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private double getDouble(DocumentSnapshot document, String field) {
        Object value = document.get(field);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        return 0;
    }

    private String buildCartDocumentId(int productId, String selectedSize) {
        return productId + "_" + normalizeSize(selectedSize).replaceAll("[^A-Za-z0-9_-]", "-");
    }

    private String normalizeSize(String selectedSize) {
        if (selectedSize == null || selectedSize.trim().isEmpty()) {
            return "default";
        }
        return selectedSize.trim();
    }

    private String getCurrentFirebaseUid() {
        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        return firebaseUser == null ? "" : firebaseUser.getUid();
    }

    private int buildStableUserId(String uid) {
        int hash = uid == null ? 0 : uid.hashCode();
        return hash == Integer.MIN_VALUE ? Integer.MAX_VALUE : Math.abs(hash);
    }
}
