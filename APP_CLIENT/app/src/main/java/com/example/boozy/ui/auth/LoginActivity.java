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
import com.example.boozy.data.model.Adresse;
import com.example.boozy.data.model.LoginResponse;
import com.example.boozy.data.model.UserLoginData;
import com.example.boozy.data.model.Utilisateur;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.client.ClientHomeActivity;
import com.example.boozy.ui.delivery.LivreurHomeActivity;
import com.google.gson.Gson;

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
        Log.d("LOGIN_TEST", "Début de performLogin()");

        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Veuillez remplir tous les champs");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        UserLoginData loginData = new UserLoginData(email, password);

        Call<LoginResponse> call = apiService.connectUser(loginData);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Utilisateur utilisateur = response.body().getUser();
                    getSharedPreferences("boozy_prefs", MODE_PRIVATE)
                            .edit()
                            .putString("email", utilisateur.getEmail())
                            .putString("password", password)
                            .apply();

                    Log.d("LOGIN_TEST", "Réponse complète : " + new Gson().toJson(response.body()));

                    Adresse adresse = utilisateur.getAdresse();
                    String userType = utilisateur.getTypeUtilisateur();
                    Log.d("LOGIN_TEST", "Type utilisateur reçu = " + userType);

                    if ("client".equalsIgnoreCase(userType)) {
                        UtilisateurManager.getInstance(getApplicationContext()).setClient(
                                utilisateur.getIdUtilisateur(),
                                utilisateur.getNom(),
                                utilisateur.getPrenom(),
                                utilisateur.getEmail(),
                                "token_placeholder"
                        );
                        UtilisateurManager.getInstance(getApplicationContext()).setPassword(password);


                        if (adresse != null) {
                            UtilisateurManager.getInstance(getApplicationContext()).setAdresse(
                                    adresse.getCivic(),
                                    adresse.getApartment(),
                                    adresse.getStreet(),
                                    adresse.getCity(),
                                    adresse.getPostalCode()
                            );
                        }

                        openActivity(ClientHomeActivity.class);

                    } else if ("carrier".equalsIgnoreCase(userType)) {
                        Log.d("LOGIN_TEST", "Ouverture LivreurHomeActivity");
                        UtilisateurManager.getInstance(getApplicationContext()).setLivreur(
                                utilisateur.getIdUtilisateur(),
                                utilisateur.getNom(),
                                utilisateur.getPrenom(),
                                utilisateur.getEmail(),
                                "token_placeholder",
                                utilisateur.getNumTel(),
                                utilisateur.getPlaqueAuto()
                        );
                        UtilisateurManager.getInstance(getApplicationContext()).setPassword(password);
                        openActivity(LivreurHomeActivity.class);

                    } else {
                        showToast("Type d'utilisateur non pris en charge");
                        Log.w("LOGIN_TEST", "Type utilisateur inconnu : " + userType);
                    }
                } else {
                    showToast("Erreur : Identifiants incorrects");
                    Log.d("LOGIN_TEST", "Réponse non réussie ou body nul");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showToast("Erreur de connexion : " + t.getMessage());
                Log.e("LOGIN_TEST", "Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        Log.d("LOGIN_TEST", "Intent vers : " + activityClass.getSimpleName());
        startActivity(new Intent(this, activityClass));
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
