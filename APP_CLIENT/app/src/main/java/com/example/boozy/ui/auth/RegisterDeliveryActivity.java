package com.example.boozy.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;

public class RegisterDeliveryActivity extends AppCompatActivity {

    private EditText inputName, inputFirstName, inputEmail, inputPhone, inputDrivingLicense, inputPassword;
    private Button buttonCreateAccountDelivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_register_delivery);
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
        inputName = findViewById(R.id.inputName);
        inputFirstName = findViewById(R.id.inputFirstName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputDrivingLicense = findViewById(R.id.inputDrivingLicense);
        inputPassword = findViewById(R.id.inputPassword);
        buttonCreateAccountDelivery = findViewById(R.id.buttonCreateAccountDelivery);

        buttonCreateAccountDelivery.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String nom = inputName.getText().toString().trim();
        String prenom = inputFirstName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String tel = inputPhone.getText().toString().trim();
        String permis = inputDrivingLicense.getText().toString().trim();
        String mdp = inputPassword.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || tel.isEmpty() || permis.isEmpty() || mdp.isEmpty()) {
            showToast("Veuillez remplir tous les champs");
            return;
        }

        if (mdp.length() < 6) {
            showToast("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        // APPEL À L'API ICI
        // Faire un appel à l'API pour enregistrer le livreur avec les informations recueillies.
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
