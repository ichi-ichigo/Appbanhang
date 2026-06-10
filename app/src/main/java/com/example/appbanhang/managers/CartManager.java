package com.example.appbanhang.managers;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.models.CartItem;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.models.User;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private static DatabaseHelper dbHelper;
    private static AuthManager authManager;
    private List<CartItem> cartItems;
    private double subtotal;
    private double shippingFee = 12000;
    private double discount = 0;

    private CartManager() {
        this.cartItems = new ArrayList<>();
        this.subtotal = 0;
    }

    // Singleton Pattern
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

    public void syncFromDatabase() {
        int userId = getCurrentUserId();
        if (dbHelper == null || userId <= 0) {
            return;
        }

        cartItems.clear();
        cartItems.addAll(dbHelper.getCartItems(userId));
        updateSubtotal();
    }

    // Add item to cart
    public void addToCart(Product product, int quantity, String selectedSize) {
        // Kiểm tra sản phẩm đã có trong giỏ chưa
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId() && 
                item.getSelectedSize().equals(selectedSize)) {
                item.setQuantity(item.getQuantity() + quantity);
                updateSubtotal();
                persistCartItem(item);
                return;
            }
        }
        // Nếu không có thì thêm mới
        CartItem newItem = new CartItem(product, quantity, selectedSize);
        cartItems.add(newItem);
        updateSubtotal();
        persistCartItem(newItem);
    }

    // Overloaded method for adding to cart with explicit parameters
    public void addToCart(int productId, String productName, double productPrice, int quantity, String selectedSize) {
        Product product = new Product(productId, productName, "", productPrice, "", "", 5.0, "");
        addToCart(product, quantity, selectedSize);
    }

    // Remove item from cart
    public void removeFromCart(CartItem item) {
        cartItems.remove(item);
        int userId = getCurrentUserId();
        if (dbHelper != null && userId > 0) {
            dbHelper.removeCartItem(userId, item.getProduct().getId(), item.getSelectedSize());
        }
        updateSubtotal();
    }

    // Update quantity
    public void updateQuantity(CartItem item, int newQuantity) {
        if (newQuantity > 0) {
            item.setQuantity(newQuantity);
            persistCartItem(item);
            updateSubtotal();
        } else {
            removeFromCart(item);
        }
    }

    // Clear cart
    public void clearCart() {
        int userId = getCurrentUserId();
        if (dbHelper != null && userId > 0) {
            dbHelper.clearCartItems(userId);
        }
        cartItems.clear();
        subtotal = 0;
        discount = 0;
    }

    // Get cart items
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    // Get all cart items (alias for getCartItems)
    public List<CartItem> getAllItems() {
        return cartItems;
    }

    // Get item count
    public int getItemCount() {
        return cartItems.size();
    }

    // Get total items (quantity sum)
    public int getTotalItemsQuantity() {
        int total = 0;
        for (CartItem item : cartItems) {
            total += item.getQuantity();
        }
        return total;
    }

    // Update subtotal
    private void updateSubtotal() {
        subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
    }

    // Get subtotal
    public double getSubtotal() {
        return subtotal;
    }

    // Get shipping fee
    public double getShippingFee() {
        return shippingFee;
    }

    // Set shipping fee
    public void setShippingFee(double fee) {
        this.shippingFee = fee;
    }

    // Apply promo code (mô phỏng)
    public void applyPromoCode(String code, double discountAmount) {
        this.discount = discountAmount;
    }

    // Get discount
    public double getDiscount() {
        return discount;
    }

    // Get total
    public double getTotal() {
        return subtotal + shippingFee - discount;
    }

    // Get total price (subtotal without shipping)
    public double getTotalPrice() {
        return subtotal;
    }

    // Is cart empty
    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    private void persistCartItem(CartItem item) {
        int userId = getCurrentUserId();
        if (dbHelper != null && userId > 0) {
            dbHelper.saveCartItem(userId, item);
        }
    }

    private int getCurrentUserId() {
        if (authManager == null) {
            return 0;
        }
        User currentUser = authManager.getCurrentUser();
        return currentUser == null ? 0 : currentUser.getId();
    }
}
