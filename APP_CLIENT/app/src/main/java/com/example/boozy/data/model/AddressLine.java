package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * AddressLine
 * -----------
 * Représente la table AddressLine pour gérer l'adresse complète
 * (si le serveur renvoie un champ "address" avec civic, apartment, street, city, postal_code).
 */
public class AddressLine implements Serializable {

    private int addressId;      // address_id
    private int civic;
    private String apartment;
    private String street;
    private String city;
    private String postalCode;

    public AddressLine() { }

    public int getAddressId() {
        return addressId;
    }
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public int getCivic() {
        return civic;
    }
    public void setCivic(int civic) {
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
