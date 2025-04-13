package com.example.boozy.data.model;

import com.google.gson.annotations.SerializedName;

public class Magasin {
    @SerializedName("shop_id")
    private int shopId;

    @SerializedName("name")
    private String name;

    @SerializedName("address")
    private String address;

    public Magasin(int shopId, String name, String address) {
        this.shopId = shopId;
        this.name = name;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
