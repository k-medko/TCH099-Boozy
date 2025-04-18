package com.example.boozy.data.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * UserAccountManager
 * ------------------
 * Gère la session utilisateur localement via SharedPreferences.
 * Cette classe sauvegarde et récupère les informations de l'utilisateur en session
 * en utilisant le modèle UserAccount.
 */
public class UtilisateurManager {

    // Nom du fichier de préférences et clés utilisées pour stocker les informations utilisateur.
    private static final String PREF_NAME = "boozy_prefs";
    private static final String KEY_ID = "id_utilisateur";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TYPE = "type_utilisateur";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_NUM_TEL = "num_tel";
    private static final String KEY_LICENSE = "license_plate";
    private static final String KEY_CAR_BRAND = "car_brand";
    private static final String KEY_ADDRESS = "user_address";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static UtilisateurManager instance;
    private Gson gson;

    /**
     * Constructeur privé (pattern Singleton) – initialise SharedPreferences.
     * @param context Le contexte de l'application.
     */
    private UtilisateurManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        gson = new Gson();
    }

    /**
     * Retourne l'instance unique de la classe.
     * @param context Le contexte de l'application.
     * @return L'instance de UserAccountManager.
     */
    public static synchronized UtilisateurManager getInstance(Context context) {
        if (instance == null) {
            instance = new UtilisateurManager(context.getApplicationContext());
        }
        return instance;
    }

    // Méthode pour stocker l'ID de l'utilisateur (par exemple, après une connexion réussie)
    public void setId(int id) {
        prefs.edit().putInt(KEY_USER_ID, id).apply();
    }

    /**
     * Sauvegarde les informations d'un utilisateur de type "client".
     * @param id L'identifiant de l'utilisateur.
     * @param nom Le nom de famille.
     * @param prenom Le prénom.
     * @param email L'email de l'utilisateur.
     * @param token Le token d'authentification.
     */
    public void setClient(int id, String nom, String prenom, String email, String token) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOM, nom);
        editor.putString(KEY_PRENOM, prenom);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TYPE, "client");
        editor.putString(KEY_TOKEN, token);
        // Pour un client, nous n'avons pas besoin des informations spécifiques aux transporteurs
        editor.remove(KEY_NUM_TEL);
        editor.remove(KEY_LICENSE);
        editor.remove(KEY_CAR_BRAND);
        editor.apply();
    }

    /**
     * Sauvegarde les informations d'un utilisateur de type "carrier".
     * @param id L'identifiant de l'utilisateur.
     * @param nom Le nom.
     * @param prenom Le prénom.
     * @param email L'email.
     * @param token Le token d'authentification.
     * @param phoneNumber Le numéro de téléphone.
     * @param licensePlate La plaque d'immatriculation.
     */
    public void setLivreur(int id, String nom, String prenom, String email, String token,
                           String phoneNumber, String licensePlate) {
        editor.putInt(KEY_ID, id);
        editor.putString(KEY_NOM, nom);
        editor.putString(KEY_PRENOM, prenom);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TYPE, "carrier");
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_NUM_TEL, phoneNumber);
        editor.putString(KEY_LICENSE, licensePlate);
        // Vous pouvez aussi sauvegarder la marque de voiture si nécessaire
        editor.apply();
    }

    /**
     * Sauvegarde l'adresse réelle de l'utilisateur dans SharedPreferences.
     * @param address L'objet AddressLine contenant l'adresse.
     */
    public void setAdresse(AddressLine address) {
        if (address != null) {
            String json = gson.toJson(address);
            editor.putString(KEY_ADDRESS, json).apply();
        }
    }

    /**
     * Récupère l'adresse sauvegardée de l'utilisateur.
     * @return Un objet AddressLine ou null si non défini.
     */
    public AddressLine getAdresse() {
        String json = prefs.getString(KEY_ADDRESS, null);
        if (json != null) {
            return gson.fromJson(json, AddressLine.class);
        }
        return null;
    }


    /**
     * Reconstruit un objet UserAccount à partir des données stockées.
     * @return Un UserAccount contenant les informations utilisateur.
     */
    public UserAccount getUserAccount() {
        UserAccount ua = new UserAccount();
        ua.setUserId(prefs.getInt(KEY_ID, -1));
        ua.setLastName(prefs.getString(KEY_NOM, ""));
        ua.setFirstName(prefs.getString(KEY_PRENOM, ""));
        ua.setEmail(prefs.getString(KEY_EMAIL, ""));
        ua.setUserType(prefs.getString(KEY_TYPE, "client"));
        ua.setPhoneNumber(prefs.getString(KEY_NUM_TEL, ""));
        ua.setLicensePlate(prefs.getString(KEY_LICENSE, ""));
        // Ajoutez d'autres champs ici si nécessaire
        return ua;
    }


    // Méthodes d'accès simples
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

    public String getUserType() {
        return prefs.getString(KEY_TYPE, "client");
    }

    public String getPhoneNumber() {
        return prefs.getString(KEY_NUM_TEL, "");
    }

    public String getLicensePlate() {
        return prefs.getString(KEY_LICENSE, "");
    }

    /**
     * Vérifie si l'utilisateur est connecté en vérifiant l'ID et le token.
     * @return true si connecté, false sinon.
     */
    public boolean isLoggedIn() {
        return (getId() != -1) && (getToken() != null && !getToken().isEmpty());
    }

    /**
     * Déconnecte l'utilisateur en vidant les données stockées.
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
}
