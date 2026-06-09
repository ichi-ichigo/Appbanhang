package com.example.appbanhang.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class Order {
    private int orderId;
    private int userId;
    private List<CartItem> items;
    private double subtotal;
    private double shippingFee;
    private double totalAmount;
    private String orderStatus; // Pending, Confirmed, Shipped, Delivered, Cancelled
    private String paymentMethod;
    private String deliveryAddress;
    private String promoCode;
    private double discount;
    private Date orderDate;
    private Date deliveryDate;

    // Constructor
    public Order(int userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        this.subtotal = 0;
        this.shippingFee = 12000; // Default shipping fee
        this.orderStatus = "Pending";
        this.orderDate = new Date();
        this.discount = 0;
    }

    // Getters & Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void addItem(CartItem item) {
        this.items.add(item);
        updateSubtotal();
    }

    public void removeItem(CartItem item) {
        this.items.remove(item);
        updateSubtotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void updateSubtotal() {
        this.subtotal = 0;
        for (CartItem item : items) {
            this.subtotal += item.getTotalPrice();
        }
        calculateTotal();
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
        calculateTotal();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    private void calculateTotal() {
        this.totalAmount = this.subtotal + this.shippingFee - this.discount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
        calculateTotal();
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
