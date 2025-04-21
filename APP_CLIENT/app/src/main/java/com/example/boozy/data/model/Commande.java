package com.example.boozy.data.model;

import java.io.Serializable;

public class Commande implements Serializable {
    private String numeroCommande;
    private String magasin;
    private String adresseLivraison;
    private String montant;
    private String dateCommande;
    private String nomLivreur;
    private int etatCommande;

    public Commande(String numeroCommande, String magasin, String adresseLivraison, String montant) {
        this.numeroCommande = numeroCommande;
        this.magasin = magasin;
        this.adresseLivraison = adresseLivraison;
        this.montant = montant;
        this.dateCommande = dateCommande;
        this.nomLivreur = nomLivreur;
        this.etatCommande = etatCommande;
    }

    public Commande(String numeroCommande, String dateCommande) {
        this.numeroCommande = numeroCommande;
        this.dateCommande = dateCommande;
        this.magasin = "";
        this.adresseLivraison = "";
        this.montant = "";
        this.nomLivreur = "";
        this.etatCommande = 0;
    }

    public String getNumeroCommande() { return numeroCommande; }
    public String getMagasin() { return magasin; }
    public String getAdresseLivraison() { return adresseLivraison; }
    public String getMontant() { return montant; }
    public String getDateCommande() { return dateCommande; }
    public void setDateCommande(String dateCommande) { this.dateCommande = dateCommande; }
    public String getNomLivreur() { return nomLivreur; }
    public void setNomLivreur(String nomLivreur) { this.nomLivreur = nomLivreur; }
    public int getEtatCommande() { return etatCommande; }
    public void setEtatCommande(int etatCommande) {
        this.etatCommande = etatCommande;
    }
}
