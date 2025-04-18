package com.example.boozy.data.model;

public enum TypeUtilisateur {
    CLIENT("client"),
    LIVREUR("carrier"),
    ADMIN("admin");

    private final String apiValue;

    TypeUtilisateur(String apiValue) {
        this.apiValue = apiValue;
    }

    /** Valeur envoyée ou reçue depuis l'API */
    public String getApiValue() { return apiValue; }

    /** Convertit la chaîne reçue depuis l'API en enum (case‑insensitive). */
    public static TypeUtilisateur fromString(String text) {
        for (TypeUtilisateur t : values()) {
            if (t.apiValue.equalsIgnoreCase(text)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Type d'utilisateur inconnu : " + text);
    }
}
