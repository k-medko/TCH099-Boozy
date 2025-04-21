package com.example.boozy.ui.delivery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.auth.AuthChoiceActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfilLivreurActivity extends AppCompatActivity {

    TextView nomText, prenomText, telephoneText, emailText, plaqueText, soldeText;
    TextView btnDeconnexion;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_livreur);

        setupFullScreen();
        initializeViews();
        setupApi();
        loadLivreurData();
        fetchSoldeDepuisServeur();

        btnDeconnexion.setOnClickListener(v -> logout());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        findViewById(R.id.btnModifierCarte).setOnClickListener(v -> {
            Intent intent = new Intent(this, ModifierProfilLivreurActivity.class);
            startActivityForResult(intent, 1001);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSoldeDepuisServeur();
    }

    private void initializeViews() {
        nomText = findViewById(R.id.nomText);
        prenomText = findViewById(R.id.prenomText);
        telephoneText = findViewById(R.id.telephoneText);
        emailText = findViewById(R.id.emailText);
        plaqueText = findViewById(R.id.plaqueText);
        soldeText = findViewById(R.id.soldeText);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);
    }

    private void setupApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);
    }

    private void loadLivreurData() {
        UtilisateurManager userManager = UtilisateurManager.getInstance(this);

        nomText.setText(userManager.getNom());
        prenomText.setText(userManager.getPrenom());
        telephoneText.setText(userManager.getNumTel());
        emailText.setText(userManager.getEmail());
        plaqueText.setText(userManager.getPlaque());
    }

    private void fetchSoldeDepuisServeur() {
        String email = UtilisateurManager.getInstance(this).getEmail();
        String password = UtilisateurManager.getInstance(this).getPassword();

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        api.connectUser(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().get("status"))) {
                    Map<String, Object> user = (Map<String, Object>) response.body().get("user");
                    double earnings = ((Number) user.get("total_earnings")).doubleValue();
                    soldeText.setText(String.format(Locale.CANADA, "%.2f$", earnings));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
            }
        });
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

    private void logout() {
        UtilisateurManager.getInstance(getApplicationContext()).logout();
        Intent intent = new Intent(this, AuthChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadLivreurData();
            fetchSoldeDepuisServeur();
        }
    }
}
