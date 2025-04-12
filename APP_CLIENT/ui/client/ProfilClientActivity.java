package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.ui.auth.AuthChoiceActivity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ProfilClientActivity extends AppCompatActivity {

    TextView nomText, prenomText, adresseText, emailText, carteText;
    TextView btnDeconnexion;
    private int userId = 123;

    private OkHttpClient okHttpClient;
    private ObjectMapper mapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_client);

        // Effet plein écran
        setupFullScreen();

        // Initialiser okhttp + Jackson
        okHttpClient= new OkHttpClient();
        mapper = new ObjectMapper();

        // Initialiser les vues
        initializeViews();

        // Charger les informations utilisateur depuis le serveur
        loadUserProfile();

        // Retour à la page précédente lorsque la flèche est cliquée
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());

        // Ouvre l’activité de modification de profil
        LinearLayout btnModifierCarte = findViewById(R.id.btnModifierCarte);
        btnModifierCarte.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModifierProfilClientActivity.class);
            startActivity(intent);
        });

        // Déconnexion
        btnDeconnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void setupFullScreen() {
        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    // Initialiser les vues
    private void initializeViews() {
        nomText = findViewById(R.id.nomText);
        prenomText = findViewById(R.id.prenomText);
        adresseText = findViewById(R.id.adresseText);
        emailText = findViewById(R.id.emailText);
        carteText = findViewById(R.id.carteText);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);  // Bouton de déconnexion
    }

    /**
     * Charger les informations utilisateur depuis le serveur
     */
    private void loadUserProfile() {
        // APPEL À L'API ICI
        // Effectuer une requête GET pour récupérer les informations utilisateur depuis le serveur.
        String url = "http://4.172.252.189:5000/getUser?userId=";

        Request rq = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(rq).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sM("ERREUR RESEAU : Impossible de charger le profil! ");
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                try{
                    if(response.isSuccessful()){
                        final String body = response.body().string()    ;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parseUserProfile(body);
                            }
                        });
                    }else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sM("ERREUR SERVEUR : Impossible de recuperer le profil.");

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
     * Affiche les champs du profil dans les TextView
     * @param json  doneee
     */
    private void parseUserProfile(String json){
        try{
            Map<?, ?> mapUser = mapper.readValue(json,Map.class);
            String nom = (String) mapUser.get("lastName");
            String prenom = (String) mapUser.get("firstName");
            String email = (String) mapUser.get("email");
            String adresse = (String) mapUser.get("address");
            String carte = (String) mapUser.get("stripeCard");

            //Remplir les TextViews
            nomText.setText(nom != null ? nom : "");
            prenomText.setText(prenom != null ? prenom : "");
            emailText.setText(email != null ? email : "");
            adresseText.setText(adresse != null ? adresse : "");
            carteText.setText(carte != null ? carte : "Aucune carte!");

        }catch (IOException e) {
            e.printStackTrace();
            sM("Erreur de parsing JSON");
        }
    }
    /**
     * Déconnexion de l'utilisateur et redirection vers la page de choix d'authentification
     */
    private void logout() {
        // A implementer : vider tokens, userId
        // Redirection vers la page de choix d'authentification
        Intent intent = new Intent(ProfilClientActivity.this, AuthChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sM(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
