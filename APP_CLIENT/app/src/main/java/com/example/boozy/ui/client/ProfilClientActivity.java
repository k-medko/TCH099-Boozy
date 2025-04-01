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

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.ui.auth.AuthChoiceActivity;


public class ProfilClientActivity extends AppCompatActivity {

    TextView nomText, prenomText, adresseText, emailText, carteText;
    TextView btnDeconnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_client);

        // Effet plein écran
        setupFullScreen();

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
        btnDeconnexion.setOnClickListener(v -> logout());
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
    }

    /**
     * Déconnexion de l'utilisateur et redirection vers la page de choix d'authentification
     */
    private void logout() {
        // A implementer

        // Redirection vers la page de choix d'authentification
        Intent intent = new Intent(ProfilClientActivity.this, AuthChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
