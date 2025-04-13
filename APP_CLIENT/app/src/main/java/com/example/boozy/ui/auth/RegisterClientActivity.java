package com.example.boozy.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.Adresse;
import com.example.boozy.data.model.Utilisateur;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterClientActivity extends AppCompatActivity {

    private EditText inputFirstName, inputLastName, inputStreetNumber, inputStreetName;
    private EditText inputAppartment, inputPostalCode, inputCity, inputEmail, inputPassword;
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
        inputAppartment = findViewById(R.id.inputAppartment);
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
        String appartment = inputAppartment.getText().toString().trim();
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

        Adresse adresse = new Adresse(streetNumber, appartment, streetName, city, postalCode);

        Utilisateur user = new Utilisateur();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setEmail(courriel);
        user.setNumTel("");
        user.setPassword(motDePasse);
        user.setTypeUtilisateur("client");
        user.setAdresse(adresse);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<Void> call = apiService.createUser(user);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    navigateToLogin();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("REGISTER_ERROR", "Code: " + response.code() + " - Body: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Réponse vide";
                        showToast("Erreur " + response.code() + ": " + error);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Erreur inconnue");
                    }

                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Erreur réseau : " + t.getMessage());
                Log.e("REGISTER_ERROR", "Erreur réseau : ", t);
            }
        });
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
