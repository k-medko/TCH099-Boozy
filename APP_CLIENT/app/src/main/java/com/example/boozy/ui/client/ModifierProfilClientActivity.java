package com.example.boozy.ui.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.CardParams;
import com.stripe.android.model.DelicateCardDetailsApi;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ModifierProfilClientActivity extends AppCompatActivity {

    private EditText editNom, editPrenom, editNumeroCivique, editRue, editCodePostal, editVille, editEmail, editPassword;
    private CardInputWidget cardInputWidget;
    private Button btnEnregistrerProfil;

    private static final String STRIPE_PUBLIC_KEY = "";
    private static final String PREFS_NAME = "boozy_prefs";
    private static final String TOKEN_KEY = "auth_token";

    private OkHttpClient okHttpClient;
    private ObjectMapper mapper;
    private static final MediaType JSON  = MediaType.parse("application/json; charset=utf-8");

    @DelicateCardDetailsApi
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_profil_client);

        setupFullScreen();

        // Initialisation Stripe
        PaymentConfiguration.init(getApplicationContext(), STRIPE_PUBLIC_KEY);

        okHttpClient  = new OkHttpClient();
        mapper = new ObjectMapper();

        initializeViews();
        loadUserData();

        btnEnregistrerProfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void setupFullScreen() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(getResources().getColor(R.color.brown));
    }

    private void initializeViews() {
        editNom = findViewById(R.id.editNom);
        editPrenom = findViewById(R.id.editPrenom);
        editNumeroCivique = findViewById(R.id.editNumeroCivique);
        editRue = findViewById(R.id.editRue);
        editCodePostal = findViewById(R.id.editCodePostal);
        editVille = findViewById(R.id.editVille);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        cardInputWidget = findViewById(R.id.cardInputWidget);
        btnEnregistrerProfil = findViewById(R.id.btnEnregistrerProfil);
    }


    /**
     * Charger les informations utilisateur depuis le serveur (GET)
     */
    private void loadUserData() {
        int userId = 123;
        String url = "http://4.172.255.120:5000/getUser/" + userId;

        Request rq = new Request.Builder()
                .url (url)
                .build();

        okHttpClient.newCall(rq).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ModifierProfilClientActivity.this,  "Erreur reseeau lors du chargement",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                try{
                    if (response.isSuccessful()){
                        final String body = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fillFields(body);
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ModifierProfilClientActivity.this, "Impossible de charger le profil",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }finally {
                    response.close();
                }
            }
        });
    }

    /**
     * Remplir les champs a partir des donnees JSON
     * @param json donnee
     */
    private void fillFields(String json){
        try {
            Map<?, ? > userMap = mapper.readValue(json, Map.class);

            String nom =  (String) userMap.get("lastName");
            String prenom = (String) userMap.get("firstName");

            String email = (String) userMap.get("email");
            String civic = (String) userMap.get("civic");
            String rue = (String) userMap.get("street");
            String cPostal = (String) userMap.get("postalCode");
            String ville = (String) userMap.get("city");

            //Remplir les EdtTExt
            if( nom != null) editNom.setText(nom);
            if( prenom != null) editPrenom.setText(prenom);
            if( email != null) editEmail.setText(email);
            if( civic != null) editNumeroCivique.setText(civic);
            if( rue != null) editRue.setText(rue);
            if( cPostal != null) editCodePostal.setText(cPostal);
            if( ville != null) editVille.setText(ville);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de l'analyse du JSON", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Sauvegarder les informations du profil utilisateur
     */
    @OptIn(markerClass = DelicateCardDetailsApi.class)
    private void saveUserProfile() {
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String civic = editNumeroCivique.getText().toString().trim();
        String rue = editRue.getText().toString().trim();
        String codePostal = editCodePostal.getText().toString().trim();
        String ville = editVille.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Vérification des champs vides
        if (nom.isEmpty() || prenom.isEmpty() || civic.isEmpty() || rue.isEmpty() ||
                codePostal.isEmpty() || ville.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupération des infos de la carte (si valide)
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
        CardParams cardParams = cardInputWidget.getCardParams();
        String resumeCarte = "Aucune carte";
        if (params != null && cardParams != null) {
            String numero = cardParams.getNumber();
            if (numero != null && numero.length() >= 4) {
                String last4 = numero.substring(numero.length() - 4);
                resumeCarte = "**** **** **** " + last4;
            }
        }

        // APPEL À L'API ICI
        // Envoyer les informations (nom, prénom, email, carte, etc.) au serveur via un appel POST.

        Map <String , Object> dataMap = new HashMap<>();

        dataMap.put("lastname", nom);
        dataMap.put("firstName", prenom);
        dataMap.put("houseNumber", civic);
        dataMap.put("street", rue);
        dataMap.put("postalCode", codePostal);
        dataMap.put("city", ville);
        dataMap.put("email", email);
        dataMap.put("stripeCard", resumeCarte);
        dataMap.put("password", password);
        dataMap.put("userType", "client");

        String jsonString;
        try{
            jsonString  = mapper.writeValueAsString(dataMap);
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this, "ERREUR: conversion JSON", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody body = RequestBody.create(JSON, jsonString);

        String url  = "http://4.172.252.189:5000/updateClient";
        Request rq = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        final String resumeCarteFinal = resumeCarte;

        okHttpClient.newCall(rq).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ModifierProfilClientActivity.this, "Erreur lors de l'enregistrement." ,
                                Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                try {


                    if (response.isSuccessful()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ModifierProfilClientActivity.this, "SUCCES! Profil enregistre.,",
                                        Toast.LENGTH_SHORT).show();

                                // Si succès, retourner à l'écran précédent
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("nom", nom);
                                resultIntent.putExtra("prenom", prenom);
                                resultIntent.putExtra("email", email);
                                resultIntent.putExtra("carte",resumeCarteFinal );
                                resultIntent.putExtra("password", password);

                                setResult(RESULT_OK, resultIntent);
                                finish();


                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ModifierProfilClientActivity.this, "Erreur : données invalides ou email déjà utilisé", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }finally {
                    response.close();
                }
            }
        });




    }

}
