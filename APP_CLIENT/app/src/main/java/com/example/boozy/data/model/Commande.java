package com.example.boozy.data.model;

import java.io.Serializable;

public class Commande implements Serializable {
    private String numeroCommande;
    private String magasin;
    private String adresseLivraison;
    private String montant;
    private String tempsEstime;
    private String dateCommande;
    private String nomLivreur;
    private int etatCommande; // 1 = traitement, 2 = en route, 3 = livrée

    public Commande(String numeroCommande, String magasin, String adresseLivraison, String montant, String tempsEstime) {
        this.numeroCommande = numeroCommande;
        this.magasin = magasin;
        this.adresseLivraison = adresseLivraison;
        this.montant = montant;
        this.tempsEstime = tempsEstime;

        this.dateCommande = dateCommande;
        this.nomLivreur = nomLivreur;
        this.etatCommande = etatCommande;
    }

    // Constructuer pour numéro et date uniquement
    public Commande(String numeroCommande, String dateCommande) {
        this.numeroCommande = numeroCommande;
        this.dateCommande = dateCommande;

        this.magasin = "";
        this.adresseLivraison = "";
        this.montant = "";
        this.tempsEstime = "";
        this.nomLivreur = "";
        this.etatCommande = 0;
    }

    public String getNumeroCommande() { return numeroCommande; }
    public String getMagasin() { return magasin; }
    public String getAdresseLivraison() { return adresseLivraison; }
    public String getMontant() { return montant; }
    public String getTempsEstime() { return tempsEstime; }
    public void setTempsEstime(String tempsEstime) { this.tempsEstime = tempsEstime; }

    public String getDateCommande() { return dateCommande; }
    public void setDateCommande(String dateCommande) { this.dateCommande = dateCommande; }

    public String getNomLivreur() { return nomLivreur; }
    public void setNomLivreur(String nomLivreur) { this.nomLivreur = nomLivreur; }

    public int getEtatCommande() { return etatCommande; }
    public void setEtatCommande(int etatCommande) {
        this.etatCommande = etatCommande;
    }
}
