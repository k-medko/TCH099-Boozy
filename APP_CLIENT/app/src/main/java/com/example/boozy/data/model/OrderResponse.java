package com.example.boozy.data.model;

public class OrderResponse {
    private int order_id;
    private String shop_address;
    private int shop_id;
    private String shop_name;
    private double total_amount;
    private String client_name;
    private String client_address;
    private String status;

    public int getOrderId() {
        return order_id;
    }

    public String getShopAddress() {
        return shop_address;
    }

    public int getShopId() {
        return shop_id;
    }

    public String getShopName() {
        return shop_name;
    }

    public double getTotalAmount() {
        return total_amount;
    }

    public String getClientName() {
        return client_name;
    }

    public String getClientAddress() {
        return client_address;
    }

    public String getStatus() {
        return status;
    }
}
