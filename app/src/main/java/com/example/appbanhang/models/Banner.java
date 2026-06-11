package com.example.appbanhang.models;

import com.google.firebase.firestore.PropertyName;

public class Banner {
    private int id;
    private String title;                      // "Summer Collection", "50% OFF"
    private String imageUrl;                   // Ảnh banner lớn
    private String actionUrl;                  // Link sản phẩm khi click
    private int displayOrder;                  // Thứ tự hiển thị
    private String type;                       // "PROMO", "BRAND", "SEASONAL"
    private boolean isActive;
    private String backgroundColor;            // Màu overlay
    private String subtitle;                   // Phụ đề banner

    // No-arg constructor required by Firestore
    public Banner() {}

    // Constructor
    public Banner(int id, String title, String imageUrl, String actionUrl, 
                  int displayOrder, String type, boolean isActive) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.actionUrl = actionUrl;
        this.displayOrder = displayOrder;
        this.type = type;
        this.isActive = isActive;
    }

    // Constructor with all fields
    public Banner(int id, String title, String imageUrl, String actionUrl,
                  int displayOrder, String type, boolean isActive, 
                  String backgroundColor, String subtitle) {
        this(id, title, imageUrl, actionUrl, displayOrder, type, isActive);
        this.backgroundColor = backgroundColor;
        this.subtitle = subtitle;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
}
