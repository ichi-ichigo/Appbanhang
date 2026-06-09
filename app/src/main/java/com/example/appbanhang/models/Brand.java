package com.example.appbanhang.models;

public class Brand {
    private int id;
    private String name;                       // "Nike", "Adidas"
    private String logoUrl;                    // Logo thương hiệu
    private String coverImageUrl;              // Ảnh nền
    private String description;

    // Constructor
    public Brand(int id, String name, String logoUrl, String coverImageUrl, 
                 String description) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.coverImageUrl = coverImageUrl;
        this.description = description;
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

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
