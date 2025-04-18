package com.example.boozy.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Adresse {

    @SerializedName("civic")
    @Expose
    private String civic;

    @SerializedName("apartment")
    @Expose
    private String apartment;

    @SerializedName("street")
    @Expose
    private String street;

    @SerializedName("city")
    @Expose
    private String city;

    @SerializedName("postal_code")
    @Expose
    private String postalCode;

    public Adresse(String civic, String apartment, String street, String city, String postalCode) {
        this.civic = civic;
        this.apartment = apartment;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
    }

    public Adresse() {}

    public String getCivic() {
        return civic;
    }

    public void setCivic(String civic) {
        this.civic = civic;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
