package com.example.boozy.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.model.Adresse;

public class RegisterClientActivity extends AppCompatActivity {

    private EditText inputFirstName, inputLastName, inputStreetNumber, inputStreetName;
    private EditText inputPostalCode, inputCity, inputEmail, inputPassword;
    private CheckBox checkboxAge;
    private Button buttonCreateAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_register_client);
        initializeViews();
    }

    private void setFullScreen() {
        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    private void initializeViews() {
        inputFirstName = findViewById(R.id.inputFirstName);
        inputLastName = findViewById(R.id.inputLastName);
        inputStreetNumber = findViewById(R.id.inputStreetNumber);
        inputStreetName = findViewById(R.id.inputStreetName);
        inputPostalCode = findViewById(R.id.inputPostalCode);
        inputCity = findViewById(R.id.inputCity);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        checkboxAge = findViewById(R.id.checkboxAge);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);

        buttonCreateAccount.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        if (!checkboxAge.isChecked()) {
            showToast("Vous devez certifier avoir 18 ans ou plus");
            return;
        }

        String nom = inputLastName.getText().toString().trim();
        String prenom = inputFirstName.getText().toString().trim();
        String streetNumber = inputStreetNumber.getText().toString().trim();
        String streetName = inputStreetName.getText().toString().trim();
        String city = inputCity.getText().toString().trim();
        String postalCode = inputPostalCode.getText().toString().trim();
        String courriel = inputEmail.getText().toString().trim();
        String motDePasse = inputPassword.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty() || courriel.isEmpty() || motDePasse.isEmpty()) {
            showToast("Veuillez remplir tous les champs obligatoires");
            return;
        }

        if (motDePasse.length() < 6) {
            showToast("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        Adresse adresse = new Adresse(0, streetNumber, streetName, postalCode, city);

        // APPEL À L'API ICI
        // Faire un appel à l'API pour enregistrer le client avec les informations recueillies.
        // Si l'enregistrement est réussi, rediriger vers la page de connexion.

        // Si l'enregistrement est réussi :
        navigateToLogin();
    }

    private void navigateToLogin() {
        showToast("Compte créé avec succès. Veuillez vous connecter.");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
