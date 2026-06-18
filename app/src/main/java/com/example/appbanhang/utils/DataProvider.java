package com.example.appbanhang.utils;

import com.example.appbanhang.models.Banner;
import com.example.appbanhang.models.Brand;
import com.example.appbanhang.models.Product;

import java.util.ArrayList;
import java.util.List;

public class DataProvider {

    public static List<Banner> getBanners() {
        List<Banner> banners = new ArrayList<>();

        banners.add(new Banner(1, "Bộ sưu tập mùa hè",
            "https://via.placeholder.com/500x200/00BCD4/ffffff?text=Summer",
            "summer_collection", 1, "SEASONAL", true, "#00BCD4", "Ưu đãi nổi bật"));

        banners.add(new Banner(2, "Giảm 50% Nike",
            "https://via.placeholder.com/500x200/8E24AA/ffffff?text=50%OFF",
            "nike_sale", 2, "PROMO", true, "#8E24AA", "Giảm giá nhanh"));

        banners.add(new Banner(3, "Sản phẩm mới",
            "https://via.placeholder.com/500x200/FF6B6B/ffffff?text=New",
            "new_arrivals", 3, "SEASONAL", true, "#FF6B6B", "Tuần này"));

        return banners;
    }

    public static List<Brand> getBrands() {
        List<Brand> brands = new ArrayList<>();

        brands.add(new Brand(1, "Nike", 
            "https://via.placeholder.com/60x60?text=Nike",
            "https://via.placeholder.com/500x200?text=Nike",
            "Cửa hàng chính hãng Nike"));

        brands.add(new Brand(2, "Adidas", 
            "https://via.placeholder.com/60x60?text=Adidas",
            "https://via.placeholder.com/500x200?text=Adidas",
            "Cửa hàng chính hãng Adidas"));

        brands.add(new Brand(3, "Puma", 
            "https://via.placeholder.com/60x60?text=Puma",
            "https://via.placeholder.com/500x200?text=Puma",
            "Cửa hàng chính hãng Puma"));

        brands.add(new Brand(4, "Converse", 
            "https://via.placeholder.com/60x60?text=Converse",
            "https://via.placeholder.com/500x200?text=Converse",
            "Cửa hàng chính hãng Converse"));

        return brands;
    }

    public static List<Product> getProducts() {
        List<Product> products = new ArrayList<>();

        // Sản phẩm 1
        List<String> images1 = new ArrayList<>();
        images1.add("https://via.placeholder.com/300x300/FF6B6B/ffffff?text=Air+Jordan");
        images1.add("https://via.placeholder.com/300x300/FF6B6B/ffffff?text=Air+Jordan+2");
        products.add(new Product(1, "Air Jordan Retro 11", "Giày thể thao",
            299.99,
            "https://via.placeholder.com/300x300/FF6B6B/ffffff?text=Air+Jordan",
            "Air Jordan Retro 11 phối màu đen đỏ cổ điển, êm chân và nổi bật khi phối đồ.",
            4.8, "Nike", images1, 20.0, "HOT DEAL", 50, "Đen/Đỏ", true));

        // Sản phẩm 2
        List<String> images2 = new ArrayList<>();
        images2.add("https://via.placeholder.com/300x300/4ECDC4/ffffff?text=Ultra+Boost");
        images2.add("https://via.placeholder.com/300x300/4ECDC4/ffffff?text=Ultra+Boost+2");
        products.add(new Product(2, "Adidas Ultra Boost", "Chạy bộ",
            250.00,
            "https://via.placeholder.com/300x300/4ECDC4/ffffff?text=Ultra+Boost",
            "Giày chạy bộ Ultra Boost nhẹ, đàn hồi tốt và phù hợp sử dụng hằng ngày.",
            4.6, "Adidas", images2, 15.0, "TRENDING", 45, "Trắng", true));

        // Sản phẩm 3
        List<String> images3 = new ArrayList<>();
        images3.add("https://via.placeholder.com/300x300/FFD700/ffffff?text=Puma+Runner");
        products.add(new Product(3, "Puma Runner Pro", "Chạy bộ",
            189.99,
            "https://via.placeholder.com/300x300/FFD700/ffffff?text=Puma+Runner",
            "Giày chạy bộ chuyên dụng với form ôm chân, hỗ trợ di chuyển linh hoạt.",
            4.5, "Puma", images3, 10.0, "NEW", 60, "Xanh dương", true));

        // Sản phẩm 4
        List<String> images4 = new ArrayList<>();
        images4.add("https://via.placeholder.com/300x300/00AA00/ffffff?text=Converse+Chuck");
        products.add(new Product(4, "Converse Chuck Taylor", "Hằng ngày",
            65.00,
            "https://via.placeholder.com/300x300/00AA00/ffffff?text=Converse+Chuck",
            "Chuck Taylor All Star cổ điển, dễ phối với nhiều phong cách.",
            4.3, "Converse", images4, 0.0, "CLASSIC", 100, "Trắng", false));

        // Sản phẩm 5
        List<String> images5 = new ArrayList<>();
        images5.add("https://via.placeholder.com/300x300/FF1493/ffffff?text=Nike+Zoom");
        products.add(new Product(5, "Nike Air Zoom", "Bóng rổ",
            349.99,
            "https://via.placeholder.com/300x300/FF1493/ffffff?text=Nike+Zoom",
            "Giày bóng rổ hiệu năng cao, hỗ trợ bám sân và giảm chấn khi bật nhảy.",
            4.9, "Nike", images5, 25.0, "HOT DEAL", 30, "Hồng/Đen", true));

        // Sản phẩm 6
        List<String> images6 = new ArrayList<>();
        images6.add("https://via.placeholder.com/300x300/FF8C00/ffffff?text=Adidas+EQT");
        products.add(new Product(6, "Adidas EQT Support", "Phong cách sống",
            220.00,
            "https://via.placeholder.com/300x300/FF8C00/ffffff?text=Adidas+EQT",
            "Giày EQT Support phong cách sống, thiết kế năng động và thoải mái.",
            4.4, "Adidas", images6, 12.0, "TRENDING", 55, "Cam", true));

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
