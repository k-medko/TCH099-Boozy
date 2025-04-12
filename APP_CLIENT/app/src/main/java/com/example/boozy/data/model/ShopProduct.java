package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * ShopProduct
 * -----------
 * Lien entre un magasin (shop_id) et un produit (product_id), plus la quantity.
 */
public class ShopProduct implements Serializable {

    private int shopId;
    private int productId;
    private int quantity;
    private Produit produit;

    public ShopProduct() { }

    public int getShopId() {
        return shopId;
    }
    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getProductId() {
        return productId;
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Produit  getProduit(){
        return produit;
    }

    public void setProduit(Produit produit){
        this.produit = produit;
    }
    public Produit getProduct() {
        return getProduit();
    }
}
