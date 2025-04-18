package com.example.boozy.ui.client;

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

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.UtilisateurManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.model.CardParams;
import com.stripe.android.model.DelicateCardDetailsApi;
import com.stripe.android.view.CardInputWidget;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ModifierProfilClientActivity extends AppCompatActivity {

    private EditText editNom, editPrenom, editNumeroCivique, editAppartement, editRue, editCodePostal, editVille, editEmail, editPassword;
    private CardInputWidget cardInputWidget;
    private Button btnEnregistrerProfil;

    private static final String STRIPE_PUBLIC_KEY = "pk_test_1234";
    private static final String BASE_URL = "http://4.172.252.189:5000/";
    private static final String PREFS_NAME = "boozy_prefs";

    @DelicateCardDetailsApi
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_profil_client);
        setupFullScreen();

        PaymentConfiguration.init(getApplicationContext(), STRIPE_PUBLIC_KEY);
        initializeViews();
        loadUserData();

        btnEnregistrerProfil.setOnClickListener(v -> saveUserProfile());
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
        editNumeroCivique = findViewById(R.id.editNumeroCivique);
        editAppartement = findViewById(R.id.editAppartment);
        editRue = findViewById(R.id.editRue);
        editCodePostal = findViewById(R.id.editCodePostal);
        editVille = findViewById(R.id.editVille);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        cardInputWidget = findViewById(R.id.cardInputWidget);
        btnEnregistrerProfil = findViewById(R.id.btnEnregistrerProfil);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String email = prefs.getString("email", "");
        String password = prefs.getString("password", "");

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Utilisateur non connecté");
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Map<String, Object>> call = apiService.connectUser(body);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Object userObj = response.body().get("user");
                    if (userObj instanceof Map) {
                        Map<String, Object> user = (Map<String, Object>) userObj;

                        editNom.setText((String) user.get("last_name"));
                        editPrenom.setText((String) user.get("first_name"));
                        editEmail.setText((String) user.get("email"));
                        editPassword.setText(password);

                        Map<String, Object> adresse = (Map<String, Object>) user.get("address");
                        if (adresse != null) {
                            Object civicObj = adresse.get("civic");
                            if (civicObj != null) {
                                if (civicObj instanceof Double) {
                                    editNumeroCivique.setText(String.valueOf(((Double) civicObj).intValue()));
                                } else {
                                    editNumeroCivique.setText(String.valueOf(civicObj));
                                }
                            }

                            editAppartement.setText((String) adresse.get("apartment"));
                            editRue.setText((String) adresse.get("street"));
                            editVille.setText((String) adresse.get("city"));
                            editCodePostal.setText((String) adresse.get("postal_code"));
                        }
                    }
                } else {
                    showToast("Erreur lors du chargement du profil");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showToast("Erreur réseau : " + t.getMessage());
            }
        });
    }

    @OptIn(markerClass = DelicateCardDetailsApi.class)
    private void saveUserProfile() {
        String nom = editNom.getText().toString().trim();
        String prenom = editPrenom.getText().toString().trim();
        String civic = editNumeroCivique.getText().toString().trim();
        String appartement = editAppartement.getText().toString().trim();
        String rue = editRue.getText().toString().trim();
        String postalCode = editCodePostal.getText().toString().trim();
        String ville = editVille.getText().toString().trim();
        String newEmail = editEmail.getText().toString().trim();
        String newPassword = editPassword.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty() || civic.isEmpty() || rue.isEmpty() ||
                postalCode.isEmpty() || ville.isEmpty() || newEmail.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String oldEmail = prefs.getString("email", "");
        String oldPassword = prefs.getString("password", "");

        if (oldEmail.isEmpty() || oldPassword.isEmpty()) {
            showToast("Utilisateur non connecté.");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", oldEmail);
        body.put("password", oldPassword);
        body.put("new_email", newEmail);
        body.put("new_password", newPassword);
        body.put("first_name", prenom);
        body.put("last_name", nom);
        body.put("phone_number", "5140000000");
        body.put("civic", civic);
        body.put("apartment", appartement); // <-- ajouté
        body.put("street", rue);
        body.put("postal_code", postalCode);
        body.put("city", ville);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Map<String, Object>> call = apiService.modifyUser(body);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    showToast("Profil mis à jour.");

                    UtilisateurManager.getInstance(getApplicationContext()).setAdresse(
                            civic,
                            appartement,
                            rue,
                            ville,
                            postalCode
                    );
                    UtilisateurManager.getInstance(getApplicationContext()).setNom(nom);
                    UtilisateurManager.getInstance(getApplicationContext()).setPrenom(prenom);
                    UtilisateurManager.getInstance(getApplicationContext()).setEmail(newEmail);

                    CardParams cardParams = cardInputWidget.getCardParams();
                    if (cardParams != null) {
                        String last4 = cardParams.getLast4();
                        UtilisateurManager.getInstance(getApplicationContext()).setCarteStripe("**** **** **** " + last4);
                    }

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("email", newEmail);
                    editor.putString("password", newPassword);
                    editor.apply();

                    setResult(RESULT_OK, new Intent());
                    finish();
                } else {
                    showToast("Erreur : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showToast("Échec réseau : " + t.getMessage());
            }
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }
}
