package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.model.Adresse;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.auth.AuthChoiceActivity;

public class ProfilClientActivity extends AppCompatActivity {

    TextView nomText, prenomText, adresseText, emailText, carteText;
    TextView btnDeconnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_client);

        setupFullScreen();
        initializeViews();
        loadUserProfile();

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.btnModifierCarte).setOnClickListener(v -> {
            startActivity(new Intent(this, ModifierProfilClientActivity.class));
        });

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

    private void initializeViews() {
        nomText = findViewById(R.id.nomText);
        prenomText = findViewById(R.id.prenomText);
        adresseText = findViewById(R.id.adresseText);
        emailText = findViewById(R.id.emailText);
        carteText = findViewById(R.id.carteText);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);
    }

    private void loadUserProfile() {
        UtilisateurManager userManager = UtilisateurManager.getInstance(this);

        nomText.setText(userManager.getNom());
        prenomText.setText(userManager.getPrenom());
        emailText.setText(userManager.getEmail());

        Adresse adr = userManager.getAdresse();
        if (adr != null) {
            String adresseComplete = adr.getCivic() + " " + adr.getStreet() + ", " +
                    adr.getCity() + ", " + adr.getPostalCode();
            adresseText.setText(adresseComplete.trim().equals(", ,") ? "Aucune adresse" : adresseComplete);
        } else {
            adresseText.setText("Aucune adresse");
        }

        String stripeCard = userManager.getCarteStripe();
        carteText.setText(stripeCard.isEmpty() ? "Aucune carte" : stripeCard);
    }

    private void logout() {
        UtilisateurManager.getInstance(getApplicationContext()).logout();
        Intent intent = new Intent(this, AuthChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
