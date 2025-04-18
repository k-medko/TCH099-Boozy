package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * RegisterDto
 * -----------
 * Objet envoyé à /createUser ou /register pour créer un compte.
 * Champs obligatoires : email, password, last_name, first_name, phone_number, user_type.
 */
public class RegisterDto implements Serializable {

    private String email;
    private String password;
    private String lastName;
    private String firstName;
    private String phoneNumber;
    private String userType;       // "client" ou "carrier"
    private String licensePlate;   // si user_type = "carrier"
    private AddressLine address;   // si on veut créer l'adresse en même temps

    public RegisterDto() { }

    // + getters / setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public AddressLine getAddress() { return address; }
    public void setAddress(AddressLine address) { this.address = address; }
}
