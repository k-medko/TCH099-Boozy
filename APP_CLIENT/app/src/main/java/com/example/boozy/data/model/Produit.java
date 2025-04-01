package com.example.boozy.data.model;

public class Produit {
    private int idProduit;
    private String name;
    private String category;
    private String description;
    private int price;
    private int imageResId; // a verifier
    private int quantity;

    // Constructeur pour ShopDetailActivity (nom, catégorie, image)
    public Produit(int idProduit, String name, String category, int imageResId) {
        this.idProduit = idProduit;
        this.name = name;
        this.category = category;
        this.imageResId = imageResId;
    }

    // Constructeur pour ProductDetailActivity (nom, prix, image, description)
    public Produit(int idProduit, String name, int price, int imageResId, String description) {
        this.idProduit = idProduit;
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.description = description;
    }

    // Constructeur pour PaiementActivity (nom, quantité)
    public Produit(int idProduit, int price, String name, int quantity) {
        this.idProduit = idProduit;
        this.price = price;
        this.name = name;
        this.quantity = quantity;
    }

    // Getters et Setters
    public int getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(int idProduit) {
        this.idProduit = idProduit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
