package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * UserAccount
 * -----------
 * Représente un compte utilisateur selon la table UserAccount de la BDD.
 * (Fusionne l'ancienne classe "Utilisateur")
 *
 * Champs correspondants :
 * - user_id, email, password, last_name, first_name, phone_number,
 *   address_id, user_type, license_plate, car_brand, total_earnings
 *
 * On peut adapter selon ce que renvoie /connectUser.
 */
public class UserAccount implements Serializable {

    private int userId;             // user_id
    private String email;
    private String password;        // uniquement si vous stockez localement
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String userType;        // "client", "carrier", "admin"
    private String licensePlate;    // si user_type = "carrier"
    private String carBrand;        // ex: "Toyota"
    private double totalEarnings;   // si user_type = "carrier", otherwise 0.0

    // (Optionnel) L'adresse, si le backend renvoie des infos d'adresse
    private AddressLine address;    // voir AddressLine.java plus bas

    // Constructeur vide requis pour la (dé)sérialisation JSON
    public UserAccount() { }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserType() {
        return userType;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getLicensePlate() {
        return licensePlate;
    }
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getCarBrand() {
        return carBrand;
    }
    public void setCarBrand(String carBrand) {
        this.carBrand = carBrand;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }
    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public AddressLine getAddress() {
        return address;
    }
    public void setAddress(AddressLine address) {
        this.address = address;
    }
}
