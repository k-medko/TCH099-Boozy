package com.example.boozy.data.model;

import android.content.Context;
import android.content.SharedPreferences;

public class UtilisateurManager {

    private static final String PREF_NAME = "boozy_prefs";
    private static final String KEY_ID = "id_utilisateur";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_TYPE = "type_utilisateur";
    private static final String KEY_TOKEN = "token";

    private static final String KEY_NUM_TEL = "num_tel";
    private static final String KEY_PLAQUE = "plaque";

    private static final String KEY_CIVIC = "civic";
    private static final String KEY_APARTMENT = "apartment";
    private static final String KEY_STREET = "street";
    private static final String KEY_CITY = "city";
    private static final String KEY_POSTAL_CODE = "postal_code";

    private static final String KEY_STRIPE_CARD = "stripe_card";

    private static final String KEY_ORDER_ID = "last_order_id";
    private static final String KEY_SHOP_NAME = "last_shop_name";
    private static final String KEY_SHOP_ADDRESS = "last_shop_address";
    private static final String KEY_TOTAL_AMOUNT = "last_total_amount";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    private static UtilisateurManager instance;

    private UtilisateurManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized UtilisateurManager getInstance(Context context) {
        if (instance == null) {
            instance = new UtilisateurManager(context);
        }
        return instance;
    }

    // ======== CLIENT ========
    public void setClient(int id, String nom, String prenom, String email, String token) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOM, nom);
        editor.putString(KEY_PRENOM, prenom);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TYPE, TypeUtilisateur.CLIENT.getType());
        editor.putString(KEY_TOKEN, token);
        editor.remove(KEY_NUM_TEL);
        editor.remove(KEY_PLAQUE);
        editor.apply();
    }

    // ======== LIVREUR ========
    public void setLivreur(int id, String nom, String prenom, String email, String token, String numTel, String plaque) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOM, nom);
        editor.putString(KEY_PRENOM, prenom);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TYPE, TypeUtilisateur.LIVREUR.getType());
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_NUM_TEL, numTel);
        editor.putString(KEY_PLAQUE, plaque);
        editor.apply();
    }

    // ======== ADRESSE ========
    public void setAdresse(String civic, String apartment, String street, String city, String postalCode) {
        editor.putString(KEY_CIVIC, civic);
        editor.putString(KEY_APARTMENT, apartment);
        editor.putString(KEY_STREET, street);
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_POSTAL_CODE, postalCode);
        editor.apply();
    }

    public Adresse getAdresse() {
        Adresse adresse = new Adresse();
        adresse.setCivic(prefs.getString(KEY_CIVIC, ""));
        adresse.setApartment(prefs.getString(KEY_APARTMENT, ""));
        adresse.setStreet(prefs.getString(KEY_STREET, ""));
        adresse.setCity(prefs.getString(KEY_CITY, ""));
        adresse.setPostalCode(prefs.getString(KEY_POSTAL_CODE, ""));
        return adresse;
    }

    // ======== STRIPE ========
    public void setCarteStripe(String cardNumber) {
        editor.putString(KEY_STRIPE_CARD, cardNumber);
        editor.apply();
    }

    public String getCarteStripe() {
        return prefs.getString(KEY_STRIPE_CARD, "");
    }

    public void clearCarteStripe() {
        editor.remove(KEY_STRIPE_CARD);
        editor.apply();
    }

    // ======== COMMANDE EN COURS (Livreur) ========
    public void setDerniereCommande(int orderId, String shopName, String shopAddress, double totalAmount) {
        editor.putInt(KEY_ORDER_ID, orderId);
        editor.putString(KEY_SHOP_NAME, shopName);
        editor.putString(KEY_SHOP_ADDRESS, shopAddress);
        editor.putFloat(KEY_TOTAL_AMOUNT, (float) totalAmount);
        editor.apply();
    }

    public boolean hasCommandeActive() {
        return prefs.getInt(KEY_ORDER_ID, -1) != -1;
    }

    public int getOrderId() {
        return prefs.getInt(KEY_ORDER_ID, -1);
    }

    public String getShopName() {
        return prefs.getString(KEY_SHOP_NAME, "");
    }

    public String getShopAddress() {
        return prefs.getString(KEY_SHOP_ADDRESS, "");
    }

    public double getTotalAmount() {
        return (double) prefs.getFloat(KEY_TOTAL_AMOUNT, 0f);
    }

    public void clearCommandeActive() {
        editor.remove(KEY_ORDER_ID);
        editor.remove(KEY_SHOP_NAME);
        editor.remove(KEY_SHOP_ADDRESS);
        editor.remove(KEY_TOTAL_AMOUNT);
        editor.apply();
    }

    // ======== GETTERS ========
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

    public String getPassword() {
        return prefs.getString(KEY_PASSWORD, "");
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    public String getNumTel() {
        return prefs.getString(KEY_NUM_TEL, "");
    }

    public String getPlaque() {
        return prefs.getString(KEY_PLAQUE, "");
    }

    public TypeUtilisateur getType() {
        String type = prefs.getString(KEY_TYPE, "client");
        return TypeUtilisateur.fromString(type);
    }

    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    // ======== LOGOUT ========
    public void logout() {
        PanierManager.getInstance(context).clearCart(); // Vide le panier à la déconnexion
        editor.clear();
        editor.apply();
    }

    // ======== SETTERS INDIVIDUELS ========
    public void setType(TypeUtilisateur typeUtilisateur) {
        editor.putString(KEY_TYPE, typeUtilisateur.getType());
        editor.apply();
    }

    public void setNom(String nom) {
        editor.putString(KEY_NOM, nom);
        editor.apply();
    }

    public void setPrenom(String prenom) {
        editor.putString(KEY_PRENOM, prenom);
        editor.apply();
    }

    public void setEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public void setPassword(String password) {
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    public void setNumTel(String numTel) {
        editor.putString(KEY_NUM_TEL, numTel);
        editor.apply();
    }

    public void setPlaque(String plaque) {
        editor.putString(KEY_PLAQUE, plaque);
        editor.apply();
    }
}
