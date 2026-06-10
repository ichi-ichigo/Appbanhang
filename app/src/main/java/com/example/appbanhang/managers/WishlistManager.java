package com.example.appbanhang.managers;

import com.example.appbanhang.database.DatabaseHelper;
import com.example.appbanhang.models.Product;
import com.example.appbanhang.models.User;

import java.util.ArrayList;
import java.util.List;

public class WishlistManager {
    private static WishlistManager instance;
    private static DatabaseHelper dbHelper;
    private static AuthManager authManager;
    private final List<Product> wishlistItems;
    private final List<Integer> favoriteProductIds;

    private WishlistManager() {
        this.wishlistItems = new ArrayList<>();
        this.favoriteProductIds = new ArrayList<>();
    }

    public static WishlistManager getInstance() {
        if (instance == null) {
            instance = new WishlistManager();
        }
        return instance;
    }

    public static void initialize(DatabaseHelper helper, AuthManager manager) {
        dbHelper = helper;
        authManager = manager;
    }

    public void addToWishlist(Product product) {
        if (product == null || isInWishlist(product.getId())) {
            return;
        }

        product.setFavorite(true);
        wishlistItems.add(product);
        int userId = getCurrentUserId();
        if (dbHelper != null && userId > 0) {
            dbHelper.addToFavorites(userId, product.getId());
        }
        if (!favoriteProductIds.contains(product.getId())) {
            favoriteProductIds.add(product.getId());
        }
    }

    public void removeFromWishlist(Product product) {
        if (product == null) {
            return;
        }

        removeFromWishlistById(product.getId());
        product.setFavorite(false);
    }

    public void removeFromWishlistById(int productId) {
        for (Product product : new ArrayList<>(wishlistItems)) {
            if (product.getId() == productId) {
                product.setFavorite(false);
                wishlistItems.remove(product);
                break;
            }
        }

        int userId = getCurrentUserId();
        if (dbHelper != null && userId > 0) {
            dbHelper.removeFromFavorites(userId, productId);
        }
        favoriteProductIds.remove(Integer.valueOf(productId));
    }

    public List<Product> getWishlistItems() {
        return wishlistItems;
    }

    public boolean isInWishlist(int productId) {
        for (Product product : wishlistItems) {
            if (product.getId() == productId) {
                return true;
            }
        }

        return favoriteProductIds.contains(productId);
    }

    public int getWishlistCount() {
        return wishlistItems.size();
    }

    public void clearWishlist() {
        wishlistItems.clear();
    }

    public void toggleWishlist(Product product) {
        if (product == null) {
            return;
        }

        if (isInWishlist(product.getId())) {
            removeFromWishlist(product);
        } else {
            addToWishlist(product);
        }
    }

    public void syncFromDatabase(List<Product> products) {
        int userId = getCurrentUserId();
        if (dbHelper == null || userId <= 0 || products == null) {
            return;
        }

        List<Integer> favoriteIds = dbHelper.getFavoriteProductIds(userId);
        favoriteProductIds.clear();
        favoriteProductIds.addAll(favoriteIds);
        wishlistItems.clear();
        for (Product product : products) {
            boolean favorite = favoriteIds.contains(product.getId());
            product.setFavorite(favorite);
            if (favorite) {
                wishlistItems.add(product);
            }
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
