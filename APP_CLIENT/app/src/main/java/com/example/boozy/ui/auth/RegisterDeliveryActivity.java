package com.example.boozy.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.Utilisateur;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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

        Utilisateur livreur = new Utilisateur();
        livreur.setNom(nom);
        livreur.setPrenom(prenom);
        livreur.setEmail(email);
        livreur.setNumTel(tel);
        livreur.setPlaqueAuto(permis);
        livreur.setPassword(mdp);
        livreur.setTypeUtilisateur("carrier");

        // Log JSON
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
        String json = gson.toJson(livreur);
        Log.d("JSON_LIVREUR", json);

        // Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.createUser(livreur);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    navigateToLogin();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Erreur inconnue";
                        Log.e("REGISTER_DELIVERY", "Erreur " + response.code() + ": " + errorBody);
                        showToast("Erreur lors de la création : " + response.code());
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("Erreur inconnue");
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Erreur réseau : " + t.getMessage());
                Log.e("REGISTER_DELIVERY", "Erreur réseau", t);
            }
        });
    }

    private void navigateToLogin() {
        showToast("Compte livreur créé avec succès.");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
