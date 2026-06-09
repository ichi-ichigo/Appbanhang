package com.example.appbanhang.managers;

import com.example.appbanhang.models.Product;
import java.util.ArrayList;
import java.util.List;

public class WishlistManager {
    private static WishlistManager instance;
    private List<Product> wishlistItems;

    private WishlistManager() {
        this.wishlistItems = new ArrayList<>();
    }

    // Singleton Pattern
    public static WishlistManager getInstance() {
        if (instance == null) {
            instance = new WishlistManager();
        }
        return instance;
    }

    // Add to wishlist
    public void addToWishlist(Product product) {
        // Kiểm tra sản phẩm đã có chưa
        if (!isInWishlist(product.getId())) {
            product.setFavorite(true);
            wishlistItems.add(product);
        }
    }

    // Remove from wishlist
    public void removeFromWishlist(Product product) {
        wishlistItems.remove(product);
        product.setFavorite(false);
    }

    // Remove by ID
    public void removeFromWishlistById(int productId) {
        for (Product product : wishlistItems) {
            if (product.getId() == productId) {
                wishlistItems.remove(product);
                product.setFavorite(false);
                break;
            }
        }
    }

    // Get all wishlist items
    public List<Product> getWishlistItems() {
        return wishlistItems;
    }

    // Check if product is in wishlist
    public boolean isInWishlist(int productId) {
        for (Product product : wishlistItems) {
            if (product.getId() == productId) {
                return true;
            }
        }
        return false;
    }

    // Get wishlist count
    public int getWishlistCount() {
        return wishlistItems.size();
    }

    // Clear wishlist
    public void clearWishlist() {
        wishlistItems.clear();
    }

    // Toggle wishlist
    public void toggleWishlist(Product product) {
        if (isInWishlist(product.getId())) {
            removeFromWishlist(product);
        } else {
            addToWishlist(product);
        }
    }
}
