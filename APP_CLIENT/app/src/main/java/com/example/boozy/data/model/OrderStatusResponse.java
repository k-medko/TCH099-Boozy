package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * OrderStatusResponse
 * -------------------
 * Représente la réponse de /getOrderStatus
 * ex: { "status": "success", "message": "...", "order_status": "InRoute" }
 */
public class OrderStatusResponse implements Serializable {
    private String status;       // "success" ou "error"
    private String message;      // "Order found" etc.
    private String orderStatus;  // "Searching","InRoute","Shipped","Cancelled","Completed"

    public OrderStatusResponse() { }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getOrderStatus() {
        return orderStatus;
    }
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
}
