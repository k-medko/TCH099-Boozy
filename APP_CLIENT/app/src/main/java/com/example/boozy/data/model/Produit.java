package com.example.boozy.data.model;

public class Produit {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private double alcohol;
    private int quantity;
    private String imageName;

    public Produit(int id, String name, String description, double price, String category, double alcohol,  int quantity, String imageName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.alcohol = alcohol;
        this.quantity = quantity;
        this.imageName = imageName;
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

    public double getAlcohol(){
        return alcohol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageName() {
        return imageName;
    }
}
