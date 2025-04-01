package com.example.boozy.data.model;

public enum TypeUtilisateur {
    CLIENT("client"),
    LIVREUR("livreur");

    private final String typeUtilisateur;

    TypeUtilisateur(String typeUtilisateur) {
        this.typeUtilisateur = typeUtilisateur;
    }

    public String getType() {
        return typeUtilisateur;
    }

    public static TypeUtilisateur fromString(String text) {
        for (TypeUtilisateur t : TypeUtilisateur.values()) {
            if (t.typeUtilisateur.equalsIgnoreCase(text)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Type d'utilisateur inconnu : " + text);
    }
}
