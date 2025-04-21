package com.example.boozy.data.model;

import com.google.gson.annotations.SerializedName;

public class Produit {

    @SerializedName("product_id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("price")
    private double price;

    @SerializedName("category")
    private String category;

    @SerializedName("shop_id")
    private String shopId;

    @SerializedName("image")
    private String imageName;

    @SerializedName("alcohol")
    private double alcohol;

    @SerializedName("stock")
    private int stock;

    private int quantity;

    public Produit(int id, String name, String description, double price, String category, int quantity, String imageName, String shopId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.quantity = quantity;
        this.imageName = imageName;
        this.shopId = shopId;
    }

    public Produit(int id, String name, String description, double price, String category, String shopId) {
        this(id, name, description, price, category, 1, null, shopId);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getShopId() {
        return shopId;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageName() {
        return imageName;
    }

    public double getAlcohol() {
        return alcohol;
    }

    public int getStock() {
        return stock;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public void setAlcohol(double alcohol) {
        this.alcohol = alcohol;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}
