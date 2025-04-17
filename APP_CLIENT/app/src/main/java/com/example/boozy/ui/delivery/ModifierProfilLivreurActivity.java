package com.example.boozy.ui.delivery;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.UtilisateurManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ModifierProfilLivreurActivity extends AppCompatActivity {

    private EditText editNom, editPrenom, editTelephone, editPlaque, editEmail, editPassword;
    private Button btnEnregistrerProfilLivreur;

    private static final String BASE_URL = "http://4.172.252.189:5000/";
    private static final String PREFS_NAME = "boozy_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_profil_livreur);
        setupFullScreen();

        initializeViews();
        loadLivreurData();

        btnEnregistrerProfilLivreur.setOnClickListener(v -> saveLivreurProfile());
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
        editNom = findViewById(R.id.editNom);
        editPrenom = findViewById(R.id.editPrenom);
        editTelephone = findViewById(R.id.editTelephone);
        editPlaque = findViewById(R.id.editPlaque);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnEnregistrerProfilLivreur = findViewById(R.id.btnEnregistrerProfilLivreur);
    }

    private void loadLivreurData() {
        UtilisateurManager userManager = UtilisateurManager.getInstance(this);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String password = prefs.getString("password", "");

        editNom.setText(userManager.getNom());
        editPrenom.setText(userManager.getPrenom());
        editTelephone.setText(userManager.getNumTel());
        editEmail.setText(userManager.getEmail());
        editPassword.setText(password);
        editPlaque.setText(userManager.getPlaque());
    }

    private void saveLivreurProfile() {
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String tel = editTelephone.getText().toString().trim();
        String plaque = editPlaque.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String oldEmail = prefs.getString("email", "");
        String oldPassword = prefs.getString("password", "");

        if (nom.isEmpty() || prenom.isEmpty() || tel.isEmpty() || plaque.isEmpty() || newEmail.isEmpty() || newPassword.isEmpty()) {
            showToast("Veuillez remplir tous les champs");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", oldEmail);
        body.put("password", oldPassword);
        body.put("new_email", newEmail);
        body.put("new_password", newPassword);
        body.put("first_name", prenom);
        body.put("last_name", nom);
        body.put("phone_number", tel);
        body.put("license_plate", plaque);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setPrettyPrinting().create()))
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Map<String, Object>> call = apiService.modifyUser(body);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    showToast("Profil mis à jour.");

                    UtilisateurManager userManager = UtilisateurManager.getInstance(getApplicationContext());
                    userManager.setNom(nom);
                    userManager.setPrenom(prenom);
                    userManager.setEmail(newEmail);
                    userManager.setNumTel(tel);
                    userManager.setPlaque(plaque);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("email", newEmail);
                    editor.putString("password", newPassword);
                    editor.apply();

                    setResult(RESULT_OK);
                    finish();
                } else {
                    showToast("Erreur " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showToast("Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
}
