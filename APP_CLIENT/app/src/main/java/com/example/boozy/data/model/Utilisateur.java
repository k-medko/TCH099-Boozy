package com.example.boozy.data.model;

import com.google.gson.annotations.SerializedName;

public class Utilisateur {

    @SerializedName("user_id")
    private int idUtilisateur;

    @SerializedName("first_name")
    private String prenom;

    @SerializedName("last_name")
    private String nom;

    @SerializedName("email")
    private String email;

    @SerializedName("phone_number")
    private String numTel;

    @SerializedName("license_plate")
    private String numeroPermis;

    @SerializedName("car_brand")
    private String marqueAuto;

    @SerializedName("total_earnings")
    private double totalGains;

    @SerializedName("user_type")
    private String typeUtilisateur;

    @SerializedName("address")
    private Adresse adresse;

    @SerializedName("password")
    private String password;

    public Utilisateur() {}

    public int getIdUtilisateur() {
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {
        this.idUtilisateur = idUtilisateur;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumTel() {
        return numTel;
    }

    public void setNumTel(String numTel) {
        this.numTel = numTel;
    }

    public String getNumeroPermis() {
        return numeroPermis;
    }

    public void setNumeroPermis(String numeroPermis) {
        this.numeroPermis = numeroPermis;
    }

    public String getMarqueAuto() {
        return marqueAuto;
    }

    public void setMarqueAuto(String marqueAuto) {
        this.marqueAuto = marqueAuto;
    }

    public double getTotalGains() {
        return totalGains;
    }

    public void setTotalGains(double totalGains) {
        this.totalGains = totalGains;
    }

    public String getTypeUtilisateur() {
        return typeUtilisateur;
    }

    public void setTypeUtilisateur(String typeUtilisateur) {
        this.typeUtilisateur = typeUtilisateur;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
