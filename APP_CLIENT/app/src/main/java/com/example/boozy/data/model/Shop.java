package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * Shop
 * ----
 * Représente un magasin (table Shop).
 * L'API /getShops renvoie un ID, un nom, et potentiellement une address.
 */
public class Shop implements Serializable {

    private int shopId;
    private String name;
    private int addressId;
    private String imageNom;

    // On peut aussi stocker une AddressLine si on veut plus de détail
    private AddressLine addressComponents; // si /getShops renvoie des parties d'adresse

    public Shop() { }

    public Shop(int shopId, String name, int addressId, String imageNom)
    {
        this.shopId  = shopId;
        this.name = name;
        this.addressId = addressId;
        this.imageNom  = imageNom;
    }
    public int getShopId() {
        return shopId;
    }
    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getAddressId() {
        return addressId;
    }
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getImageNom(){
        return imageNom;
    }

    public void setImageNom(String imageNom){
        this.imageNom = imageNom;
    }
}
