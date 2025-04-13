package com.example.boozy.data.model;

public enum TypeUtilisateur {
    CLIENT("client"),
    LIVREUR("carrier");

    private final String type;

    TypeUtilisateur(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static TypeUtilisateur fromString(String type) {
        if (type == null) return CLIENT;
        for (TypeUtilisateur t : values()) {
            if (t.getType().equalsIgnoreCase(type)) {
                return t;
            }
        }
        return CLIENT;
    }
}
