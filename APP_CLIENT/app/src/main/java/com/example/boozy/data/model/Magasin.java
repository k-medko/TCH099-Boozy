package com.example.boozy.data.model;

public class Magasin {
    private int store_id;
    private String name;
    private int address_id;
    private String image_nom;

    public Magasin(int store_id, String name, int address_id, String image_nom) {
        this.store_id = store_id;
        this.name = name;
        this.address_id = address_id;
        this.image_nom = image_nom;
    }

    public int getStoreId() {
        return store_id;
    }

    public void setStoreId(int store_id) {
        this.store_id = store_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAddressId() {
        return address_id;
    }

    public void setAddressId(int address_id) {
        this.address_id = address_id;
    }

    public String getImageNom() {
        return image_nom;
    }

    public void setImageNom(String image_nom) {
        this.image_nom = image_nom;
    }
}
