package com.example.boozy.data.model;

import android.content.Context;
import android.content.SharedPreferences;

public class UtilisateurManager {

    private static final String PREF_NAME = "boozy_prefs";

    private static final String KEY_ID = "id_utilisateur";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TYPE = "type_utilisateur";
    private static final String KEY_TOKEN = "token";

    private static final String KEY_NUM_TEL = "num_tel";
    private static final String KEY_NUMERO_PERMIS = "numero_permis";

    private static final String KEY_NUMERO_CIVIQUE = "numero_civique";
    private static final String KEY_RUE = "rue";
    private static final String KEY_CODE_POSTAL = "code_postal";
    private static final String KEY_VILLE = "ville";

    private static final String KEY_STRIPE_CARD = "stripe_card";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    private static UtilisateurManager instance;

    private UtilisateurManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized UtilisateurManager getInstance(Context context) {
        if (instance == null) {
            instance = new UtilisateurManager(context.getApplicationContext());
        }
        return instance;
    }

    // Pour client
    public void setClient(int id, String nom, String prenom, String email, String token) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOM, nom);
        editor.putString(KEY_PRENOM, prenom);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TYPE, TypeUtilisateur.CLIENT.getType());
        editor.putString(KEY_TOKEN, token);
        editor.remove(KEY_NUM_TEL);
        editor.remove(KEY_NUMERO_PERMIS);
        editor.apply();
    }

    // Pour livreur
    public void setLivreur(int id, String nom, String prenom, String email, String token, String numTel, String numeroPermis) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOM, nom);
        editor.putString(KEY_PRENOM, prenom);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TYPE, TypeUtilisateur.LIVREUR.getType());
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_NUM_TEL, numTel);
        editor.putString(KEY_NUMERO_PERMIS, numeroPermis);
        editor.apply();
    }

    // Enregistrer l'adresse
    public void setAdresse(String numeroCivique, String rue, String codePostal, String ville) {
        editor.putString(KEY_NUMERO_CIVIQUE, numeroCivique);
        editor.putString(KEY_RUE, rue);
        editor.putString(KEY_CODE_POSTAL, codePostal);
        editor.putString(KEY_VILLE, ville);
        editor.apply();
    }

    // Récupérer l'adresse
    public Adresse getAdresse() {
        String numeroCivique = prefs.getString(KEY_NUMERO_CIVIQUE, "");
        String rue = prefs.getString(KEY_RUE, "");
        String codePostal = prefs.getString(KEY_CODE_POSTAL, "");
        String ville = prefs.getString(KEY_VILLE, "");
        return new Adresse(0, numeroCivique, rue, codePostal, ville);
    }

    // Enregistrer la carte Stripe
    public void setCarteStripe(String cardNumber) {
        editor.putString(KEY_STRIPE_CARD, cardNumber);
        editor.apply();
    }

    // Récupérer la carte Stripe
    public String getCarteStripe() {
        return prefs.getString(KEY_STRIPE_CARD, "");
    }

    // Supprimer la carte Stripe
    public void clearCarteStripe() {
        editor.remove(KEY_STRIPE_CARD);
        editor.apply();
    }

    public int getId() {
        return prefs.getInt(KEY_ID, -1);
    }

    public String getNom() {
        return prefs.getString(KEY_NOM, "");
    }

    public String getPrenom() {
        return prefs.getString(KEY_PRENOM, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public String getNumTel() {
        return prefs.getString(KEY_NUM_TEL, "");
    }

    public String getNumeroPermis() {
        return prefs.getString(KEY_NUMERO_PERMIS, "");
    }

    public TypeUtilisateur getType() {
        String type = prefs.getString(KEY_TYPE, "client");
        return TypeUtilisateur.fromString(type);
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public void setType(TypeUtilisateur typeUtilisateur) {
        editor.putString(KEY_TYPE, typeUtilisateur.getType());
        editor.apply();
    }
}
