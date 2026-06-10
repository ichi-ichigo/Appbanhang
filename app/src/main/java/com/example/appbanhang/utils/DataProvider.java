package com.example.appbanhang.utils;

import com.example.appbanhang.models.Banner;
import com.example.appbanhang.models.Brand;
import com.example.appbanhang.models.Product;

import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    public static List<Banner> getBanners() {
        List<Banner> banners = new ArrayList<>();

        banners.add(new Banner(1, "Summer Collection", 
            "https://via.placeholder.com/500x200/00BCD4/ffffff?text=Summer",
            "summer_collection", 1, "SEASONAL", true, "#00BCD4", "Hot Deal"));

        banners.add(new Banner(2, "50% OFF Nike", 
            "https://via.placeholder.com/500x200/8E24AA/ffffff?text=50%OFF",
            "nike_sale", 2, "PROMO", true, "#8E24AA", "Flash Sale"));

        banners.add(new Banner(3, "New Arrivals",
            "https://via.placeholder.com/500x200/FF6B6B/ffffff?text=New",
            "new_arrivals", 3, "SEASONAL", true, "#FF6B6B", "This Week"));

        return banners;
    }

    public static List<Brand> getBrands() {
        List<Brand> brands = new ArrayList<>();

        brands.add(new Brand(1, "Nike", 
            "https://via.placeholder.com/60x60?text=Nike",
            "https://via.placeholder.com/500x200?text=Nike",
            "Nike Official Store"));

        brands.add(new Brand(2, "Adidas", 
            "https://via.placeholder.com/60x60?text=Adidas",
            "https://via.placeholder.com/500x200?text=Adidas",
            "Adidas Official Store"));

        brands.add(new Brand(3, "Puma", 
            "https://via.placeholder.com/60x60?text=Puma",
            "https://via.placeholder.com/500x200?text=Puma",
            "Puma Official Store"));

        brands.add(new Brand(4, "Converse", 
            "https://via.placeholder.com/60x60?text=Converse",
            "https://via.placeholder.com/500x200?text=Converse",
            "Converse Official Store"));

        return brands;
    }

    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();

        // Product 1
        List<String> images1 = new ArrayList<>();
        images1.add("https://via.placeholder.com/300x300/FF6B6B/ffffff?text=Air+Jordan");
        images1.add("https://via.placeholder.com/300x300/FF6B6B/ffffff?text=Air+Jordan+2");
        products.add(new Product(1, "Air Jordan Retro 11", "Sneakers",
            299.99,
            "https://via.placeholder.com/300x300/FF6B6B/ffffff?text=Air+Jordan",
            "Classic Air Jordan Retro 11 Black/Red",
            4.8, "Nike", images1, 20.0, "HOT DEAL", 50, "Black/Red", true));

        // Product 2
        List<String> images2 = new ArrayList<>();
        images2.add("https://via.placeholder.com/300x300/4ECDC4/ffffff?text=Ultra+Boost");
        images2.add("https://via.placeholder.com/300x300/4ECDC4/ffffff?text=Ultra+Boost+2");
        products.add(new Product(2, "Adidas Ultra Boost", "Running",
            250.00,
            "https://via.placeholder.com/300x300/4ECDC4/ffffff?text=Ultra+Boost",
            "Ultra Boost Running Shoes",
            4.6, "Adidas", images2, 15.0, "TRENDING", 45, "White", true));

        // Product 3
        List<String> images3 = new ArrayList<>();
        images3.add("https://via.placeholder.com/300x300/FFD700/ffffff?text=Puma+Runner");
        products.add(new Product(3, "Puma Runner Pro", "Running",
            189.99,
            "https://via.placeholder.com/300x300/FFD700/ffffff?text=Puma+Runner",
            "Professional Running Shoes",
            4.5, "Puma", images3, 10.0, "NEW", 60, "Blue", true));

        // Product 4
        List<String> images4 = new ArrayList<>();
        images4.add("https://via.placeholder.com/300x300/00AA00/ffffff?text=Converse+Chuck");
        products.add(new Product(4, "Converse Chuck Taylor", "Casual",
            65.00,
            "https://via.placeholder.com/300x300/00AA00/ffffff?text=Converse+Chuck",
            "Classic Chuck Taylor All Star",
            4.3, "Converse", images4, 0.0, "CLASSIC", 100, "White", false));

        // Product 5
        List<String> images5 = new ArrayList<>();
        images5.add("https://via.placeholder.com/300x300/FF1493/ffffff?text=Nike+Zoom");
        products.add(new Product(5, "Nike Air Zoom", "Basketball",
            349.99,
            "https://via.placeholder.com/300x300/FF1493/ffffff?text=Nike+Zoom",
            "Basketball Performance Shoes",
            4.9, "Nike", images5, 25.0, "HOT DEAL", 30, "Pink/Black", true));

        // Product 6
        List<String> images6 = new ArrayList<>();
        images6.add("https://via.placeholder.com/300x300/FF8C00/ffffff?text=Adidas+EQT");
        products.add(new Product(6, "Adidas EQT Support", "Lifestyle",
            220.00,
            "https://via.placeholder.com/300x300/FF8C00/ffffff?text=Adidas+EQT",
            "EQT Support Lifestyle Shoes",
            4.4, "Adidas", images6, 12.0, "TRENDING", 55, "Orange", true));

        return products;
    }

    /**
     * Lấy sản phẩm nổi bật (có NEW, HOT DEAL, TRENDING)
     */
    public static List<Product> getFeaturedProducts() {
        List<Product> allProducts = getProducts();
        List<Product> featured = new ArrayList<>();
        
        for (Product p : allProducts) {
            if (p.getPromotion() != null && 
                (p.getPromotion().equals("HOT DEAL") || 
                 p.getPromotion().equals("TRENDING") ||
                 p.getPromotion().equals("NEW"))) {
                featured.add(p);
            }
        }
        
        return featured;
    }

    public static Product getProductById(int productId) {
        for (Product product : getProducts()) {
            if (product.getId() == productId) {
                return product;
            }
        }
        return null;
    }
}
