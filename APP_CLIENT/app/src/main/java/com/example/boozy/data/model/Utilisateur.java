package com.example.boozy.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

    public class Utilisateur {

        @SerializedName("user_id")
        @Expose(serialize = false)
        private int idUtilisateur;

        @SerializedName("first_name")
        @Expose
        private String prenom;

        @SerializedName("last_name")
        @Expose
        private String nom;

        @SerializedName("email")
        @Expose
        private String email;

        @SerializedName("password")
        @Expose
        private String password;

        @SerializedName("phone_number")
        @Expose
        private String numTel;

        @SerializedName("user_type")
        @Expose
        private String typeUtilisateur;

        @SerializedName("address")
        @Expose
        private Adresse adresse;

        @SerializedName("license_plate")
        @Expose
        private String plaqueAuto;

        @SerializedName("car_brand")
        @Expose
        private String marqueVoiture;

        public Utilisateur() {}

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

        public int getIdUtilisateur() {
            return idUtilisateur;
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

        public String getNumTel() {
            return numTel;
        }

        public void setNumTel(String numTel) {
            this.numTel = numTel;
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

        public String getPlaqueAuto() {
            return plaqueAuto;
        }

        public void setPlaqueAuto(String plaqueAuto) {
            this.plaqueAuto = plaqueAuto;
        }

        public String getCarBrand() {
            return marqueVoiture;
        }

        public void setCarBrand(String marqueVoiture) {
            this.marqueVoiture = marqueVoiture;
        }
    }
