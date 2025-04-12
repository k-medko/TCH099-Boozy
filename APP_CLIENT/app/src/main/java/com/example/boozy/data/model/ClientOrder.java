package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * ClientOrder
 * -----------
 * Représente la table ClientOrder côté client.
 * Permet de créer une commande (POST /createOrder)
 * ou de recevoir une commande depuis /getOrderHistory, etc.
 */
public class ClientOrder implements Serializable {

    // On peut stocker un ID numerique (client_order_id) ou un string (#CMDxxx).
    private int clientOrderId;        // ID numérique
    private String creationDate;      // "2025-04-27 14:30:00"
    private String status;            // "Searching","InRoute","Shipped","Cancelled","Completed"
    private double totalAmount;       // total_amount
    private int addressId;            // si on stocke l'ID, ou un object AddressLine
    private int shopId;               // ID du magasin
    private int clientId;             // ID du client
    private int carrierId;            // ID du livreur



    private String shopAddress;
    private String clientAddress;

    private String tempsEstime;

    // Constructeur vide
    public ClientOrder() { }

    // Constructeur complet
    public ClientOrder(int clientOrderId, String creationDate, String status,
                       double totalAmount, int addressId, int shopId,
                       int clientId, int carrierId) {
        this.clientOrderId = clientOrderId;
        this.creationDate = creationDate;
        this.status = status;
        this.totalAmount = totalAmount;
        this.addressId = addressId;
        this.shopId = shopId;
        this.clientId = clientId;
        this.carrierId = carrierId;
    }

    // + getters / setters
    public int getClientOrderId() {
        return clientOrderId;
    }
    public void setClientOrderId(int clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public String getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getAddressId() {
        return addressId;
    }
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getShopId() {
        return shopId;
    }
    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getClientId() {
        return clientId;
    }
    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getCarrierId() {
        return carrierId;
    }
    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }

    public String getTempsEstime() {
        return tempsEstime;
    }

    public void setTempsEstime(String tempsEstime) {
        this.tempsEstime = tempsEstime;
    }


    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }
}
