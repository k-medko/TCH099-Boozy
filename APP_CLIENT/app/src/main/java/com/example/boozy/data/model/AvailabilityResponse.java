package com.example.boozy.data.model;

import com.google.gson.annotations.SerializedName;

public class AvailabilityResponse {

    @SerializedName("product_id")
    private int productId;

    @SerializedName("shop_id")
    private int shopId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("product")
    private ProductDetail product;

    @SerializedName("shop")
    private ShopDetail shop;

    public int getProductId() {
        return productId;
    }

    public int getShopId() {
        return shopId;
    }

    public int getQuantity() {
        return quantity;
    }

    public ProductDetail getProduct() {
        return product;
    }

    public ShopDetail getShop() {
        return shop;
    }

    public static class ProductDetail {

        @SerializedName("name")
        private String name;

        @SerializedName("price")
        private double price;

        @SerializedName("description")
        private String description;

        @SerializedName("category")
        private String category;

        @SerializedName("alcohol")
        private double alcohol;

        @SerializedName("image")
        private String image;

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public String getDescription() {
            return description;
        }

        public String getCategory() {
            return category;
        }

        public double getAlcohol() {
            return alcohol;
        }

        public String getImage() {
            return image;
        }
    }

    public static class ShopDetail {

        @SerializedName("name")
        private String name;

        @SerializedName("address")
        private String address;

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }
    }
}
