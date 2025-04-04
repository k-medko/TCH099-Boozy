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
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.TypeUtilisateur;
import com.example.boozy.data.model.UserLoginData;
import com.example.boozy.data.model.Utilisateur;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.client.ClientHomeActivity;
import com.example.boozy.ui.delivery.LivreurHomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_login);
        initializeViews();

        // Vérifier si l'utilisateur est déjà connecté
        if (UtilisateurManager.getInstance(getApplicationContext()).isLoggedIn()) {
            if (UtilisateurManager.getInstance(getApplicationContext()).getType() == TypeUtilisateur.CLIENT) {
                openActivity(ClientHomeActivity.class);
            } else {
                openActivity(LivreurHomeActivity.class);
            }
        }
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
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Veuillez remplir tous les champs");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.255.120:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        UserLoginData loginData = new UserLoginData(email, password);

        Call<Utilisateur> call = apiService.connectUser(loginData);
        call.enqueue(new Callback<Utilisateur>() {
            @Override
            public void onResponse(Call<Utilisateur> call, Response<Utilisateur> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Utilisateur utilisateur = response.body();
                    String userType = utilisateur.getTypeUtilisateur();

                    // Enregistrer les informations utilisateur localement
                    if (userType.equals("customer")) {
                        UtilisateurManager.getInstance(getApplicationContext()).setClient(
                                utilisateur.getIdUtilisateur(),
                                utilisateur.getNom(),
                                utilisateur.getPrenom(),
                                utilisateur.getEmail(),
                                "token_placeholder"
                        );
                        openActivity(ClientHomeActivity.class);
                    } else if (userType.equals("deliverer")) {
                        UtilisateurManager.getInstance(getApplicationContext()).setLivreur(
                                utilisateur.getIdUtilisateur(),
                                utilisateur.getNom(),
                                utilisateur.getPrenom(),
                                utilisateur.getEmail(),
                                "token_placeholder",
                                utilisateur.getNumTel(),
                                utilisateur.getNumeroPermis()
                        );
                        openActivity(LivreurHomeActivity.class);
                    } else if (userType.equals("admin")) {
                        showToast("Connexion admin non implémentée");
                    } else {
                        showToast("Type d'utilisateur non reconnu");
                    }
                } else {
                    showToast("Erreur : Identifiants incorrects.");
                }
            }

            @Override
            public void onFailure(Call<Utilisateur> call, Throwable t) {
                showToast("Erreur de connexion : " + t.getMessage());
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
