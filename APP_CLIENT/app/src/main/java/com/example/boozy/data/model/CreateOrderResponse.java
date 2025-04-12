package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * CreateOrderResponse
 * -------------------
 * Représente la réponse de /createOrder.
 * L'API renvoie par exemple {"status": "success", "order_id": 123}.
 */
public class CreateOrderResponse implements Serializable {
    private String status;  // "success" ou "error"
    private int orderId;

    public CreateOrderResponse() { }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
