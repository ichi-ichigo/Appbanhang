package com.example.appbanhang.models;

public class CartItem {
    private int cartItemId;
    private Product product;
    private int quantity;
    private String selectedSize;
    private double totalPrice;

    // Constructor
    public CartItem(Product product, int quantity, String selectedSize) {
        this.product = product;
        this.quantity = quantity;
        this.selectedSize = selectedSize;
        this.totalPrice = product.getPrice() * quantity;
    }

    // Getters & Setters
    public int getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(int cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotalPrice();
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void updateTotalPrice() {
        this.totalPrice = product.getPrice() * quantity;
    }
}
