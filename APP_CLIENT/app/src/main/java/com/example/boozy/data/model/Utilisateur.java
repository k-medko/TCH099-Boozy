package com.example.boozy.data.model;

import com.google.gson.annotations.SerializedName;

public class Utilisateur {
    @SerializedName("userId")
    private int idUtilisateur;

    @SerializedName("firstName")
    private String prenom;

    @SerializedName("lastName")
    private String nom;

    @SerializedName("email")
    private String email;

    @SerializedName("phoneNumber")
    private String numTel;

    @SerializedName("licensePlate")
    private String numeroPermis;

    @SerializedName("userType")
    private String typeUtilisateur;

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

    public String getTypeUtilisateur() {
        return typeUtilisateur;
    }

    public void setTypeUtilisateur(String typeUtilisateur) {
        this.typeUtilisateur = typeUtilisateur;
    }
}
