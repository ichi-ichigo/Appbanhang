package com.example.appbanhang.models;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int id;
    private String name;
    private String category;
    private double price;
    private String imageUrl;
    private String description;
    private double rating;
    private int reviewCount;
    private String brand;
    private boolean isFavorite;
    
    // New fields from DYNAMIC_CONTENT_PLAN
    private List<String> imageUrls;
    private String thumbnailUrl;
    private double discount;                   // % giảm giá
    private String promotion;                  // "NEW", "HOT DEAL", "TRENDING"
    private int stock;                         // Số lượng còn
    private String color;                      // Màu sắc chính
    private boolean isNew;                     // Sản phẩm mới

    // Constructor - with basic fields
    public Product(int id, String name, String category, double price, 
                   String imageUrl, String description, double rating, 
                   String brand) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.rating = rating;
        this.brand = brand;
        this.isFavorite = false;
        this.imageUrls = new ArrayList<>();
        this.discount = 0;
        this.stock = 0;
        this.isNew = false;
    }

    // Constructor - with extended fields
    public Product(int id, String name, String category, double price, 
                   String imageUrl, String description, double rating, 
                   String brand, List<String> imageUrls, double discount, 
                   String promotion, int stock, String color, boolean isNew) {
        this(id, name, category, price, imageUrl, description, rating, brand);
        this.imageUrls = imageUrls;
        this.thumbnailUrl = imageUrl;
        this.discount = discount;
        this.promotion = promotion;
        this.stock = stock;
        this.color = color;
        this.isNew = isNew;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    // New getters & setters
    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }
}
