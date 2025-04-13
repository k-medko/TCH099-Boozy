package com.example.boozy.data.model;

import com.google.gson.annotations.SerializedName;

public class Adresse {

    @SerializedName("civic")
    private String civic;

    @SerializedName("apartment")
    private String apartment;

    @SerializedName("street")
    private String street;

    @SerializedName("city")
    private String city;

    @SerializedName("postal_code")
    private String postalCode;

    // Constructeur avec paramètres
    public Adresse(String civic, String apartment, String street, String city, String postalCode) {
        this.civic = civic;
        this.apartment = apartment;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
    }

    // Constructeur vide
    public Adresse() {}

    // Getters
    public String getCivic() {
        return civic;
    }

    public String getApartment() {
        return apartment;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setCivic(String civic) {
        this.civic = civic;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

}
