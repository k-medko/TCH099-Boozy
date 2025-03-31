package com.example.boozy.ui.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.CardParams;
import com.stripe.android.model.DelicateCardDetailsApi;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

public class ModifierProfilClientActivity extends AppCompatActivity {

    private EditText editNom, editPrenom, editNumeroCivique, editRue, editCodePostal, editVille, editEmail, editPassword;
    private CardInputWidget cardInputWidget;
    private Button btnEnregistrerProfil;

    private static final String STRIPE_PUBLIC_KEY = "";
    private static final String PREFS_NAME = "boozy_prefs";
    private static final String TOKEN_KEY = "auth_token";

    @DelicateCardDetailsApi
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_profil_client);

        setupFullScreen();

        // Initialisation Stripe
        PaymentConfiguration.init(getApplicationContext(), STRIPE_PUBLIC_KEY);

        initializeViews();
        loadUserData();

        btnEnregistrerProfil.setOnClickListener(v -> saveUserProfile());
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
     * Charger les informations utilisateur depuis le serveur
     */
    private void loadUserData() {
        // APPEL À L'API ICI
        // Récupérer les informations de profil depuis le serveur via un appel GET.

        // Remplir les champs avec les données récupérées depuis l'API
        // editNom.setText(nom);
        // editPrenom.setText(prenom);
        // editEmail.setText(email);
        // etc.
    }

    /**
     * Sauvegarder les informations du profil utilisateur
     */
    @OptIn(markerClass = DelicateCardDetailsApi.class)
    private void saveUserProfile() {
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String numeroCivique = editNumeroCivique.getText().toString().trim();
        String rue = editRue.getText().toString().trim();
        String codePostal = editCodePostal.getText().toString().trim();
        String ville = editVille.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Vérification des champs vides
        if (nom.isEmpty() || prenom.isEmpty() || numeroCivique.isEmpty() || rue.isEmpty() ||
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
        try {
            // Appel API ici
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show());
            return;
        }

        // Si succès, retourner à l'écran précédent
        Intent resultIntent = new Intent();
        resultIntent.putExtra("nom", nom);
        resultIntent.putExtra("prenom", prenom);
        resultIntent.putExtra("email", email);
        resultIntent.putExtra("carte", resumeCarte);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
