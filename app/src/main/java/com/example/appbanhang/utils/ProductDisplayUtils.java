package com.example.appbanhang.utils;

public final class ProductDisplayUtils {
    private ProductDisplayUtils() {
    }

    public static String category(String value) {
        if (isBlank(value)) {
            return "Chưa phân loại";
        }

        switch (value.trim().toLowerCase()) {
            case "running":
            case "chạy bộ":
                return "Chạy bộ";
            case "sneakers":
            case "giày thể thao":
                return "Giày thể thao";
            case "basketball":
            case "bóng rổ":
                return "Bóng rổ";
            case "casual":
            case "hằng ngày":
                return "Hằng ngày";
            case "lifestyle":
            case "phong cách sống":
                return "Phong cách sống";
            default:
                return value;
        }
    }

    public static String promotion(String value) {
        if (isBlank(value)) {
            return "";
        }

        switch (value.trim().toUpperCase()) {
            case "HOT DEAL":
                return "Ưu đãi hot";
            case "TRENDING":
                return "Xu hướng";
            case "NEW":
                return "Mới";
            case "CLASSIC":
                return "Cổ điển";
            default:
                return value;
        }
    }

    public static String description(String value) {
        if (isBlank(value)) {
            return "Chưa có mô tả cho sản phẩm này.";
        }

        switch (value.trim()) {
            case "Classic Air Jordan Retro 11 Black/Red":
                return "Air Jordan Retro 11 phối màu đen đỏ cổ điển, êm chân và nổi bật khi phối đồ.";
            case "Ultra Boost Running Shoes":
                return "Giày chạy bộ Ultra Boost nhẹ, đàn hồi tốt và phù hợp sử dụng hằng ngày.";
            case "Professional Running Shoes":
                return "Giày chạy bộ chuyên dụng với form ôm chân, hỗ trợ di chuyển linh hoạt.";
            case "Classic Chuck Taylor All Star":
                return "Chuck Taylor All Star cổ điển, dễ phối với nhiều phong cách.";
            case "Basketball Performance Shoes":
                return "Giày bóng rổ hiệu năng cao, hỗ trợ bám sân và giảm chấn khi bật nhảy.";
            case "EQT Support Lifestyle Shoes":
                return "Giày EQT Support phong cách sống, thiết kế năng động và thoải mái.";
            default:
                return value;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
