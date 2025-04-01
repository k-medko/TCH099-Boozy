package com.example.boozy.data.model;

public class Adresse {
    private int idAdresse;
    private String numeroCivique;
    private String rue;
    private String codePostal;
    private String ville;

    public Adresse(int idAdresse, String numeroCivique, String rue, String codePostal, String ville) {
        this.idAdresse = idAdresse;
        this.numeroCivique = numeroCivique;
        this.rue = rue;
        this.codePostal = codePostal;
        this.ville = ville;
    }

    // Getters et setters
    public int getIdAdresse() {
        return idAdresse;
    }

    public void setIdAdresse(int idAdresse) {
        this.idAdresse = idAdresse;
    }

    public String getNumeroCivique() {
        return numeroCivique;
    }

    public void setNumeroCivique(String numeroCivique) {
        this.numeroCivique = numeroCivique;
    }

    public String getRue() {
        return rue;
    }

    public void setRue(String rue) {
        this.rue = rue;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

}
