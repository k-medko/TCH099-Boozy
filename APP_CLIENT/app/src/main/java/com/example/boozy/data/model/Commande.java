package com.example.boozy.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Commande implements Serializable {

    @SerializedName("order_id")
    private int orderId;

    @SerializedName("creation_date")
    private String creationDate;

    @SerializedName("status")
    private String status;

    @SerializedName("carrier_name")
    private String carrierName;

    @SerializedName("shop_id")
    private int shopId;

    private String numeroCommande;
    private String magasin;
    private String adresseLivraison;
    private String montant;
    private String dateCommande;
    private int etatCommande;

    public int getOrderId() {
        return orderId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getStatus() {
        return status;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public int getShopId() {
        return shopId;
    }

    public String getNumeroCommande() {
        return numeroCommande;
    }

    public String getMagasin() {
        return magasin;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public String getMontant() {
        return montant;
    }

    public String getDateCommande() {
        return dateCommande != null ? dateCommande : creationDate;
    }

    public int getEtatCommande() {
        return etatCommande;
    }

    public void setNumeroCommande(String numeroCommande) {
        this.numeroCommande = numeroCommande;
    }

    public void setMagasin(String magasin) {
        this.magasin = magasin;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }

    public void setDateCommande(String dateCommande) {
        this.dateCommande = dateCommande;
    }

    public void setEtatCommande(int etatCommande) {
        this.etatCommande = etatCommande;
    }

    public Commande(String numeroCommande, String magasin, String adresseLivraison, String montant) {
        this.numeroCommande = numeroCommande;
        this.magasin = magasin;
        this.adresseLivraison = adresseLivraison;
        this.montant = montant;
    }

}
