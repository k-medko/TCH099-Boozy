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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterClientActivity extends AppCompatActivity {

    private EditText inputFirstName, inputLastName, inputStreetNumber, inputStreetName;
    private EditText inputPostalCode, inputCity, inputEmail, inputPassword;
    private CheckBox checkboxAge;
    private Button buttonCreateAccount;

    private OkHttpClient okHttpClient;

    private ObjectMapper mapper;

    private static final MediaType JSON = MediaType.parse("application/json;  charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_register_client);

        //init OkHttp client
        okHttpClient = new OkHttpClient();
        //init Jackson
        mapper = new ObjectMapper();

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

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });
    }

    //voir si le  USER a 18 ans
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


        //valider les champs obligatoires
        if (nom.isEmpty() || prenom.isEmpty() || courriel.isEmpty() || motDePasse.isEmpty()) {
            showToast("Veuillez remplir tous les champs obligatoires");
            return;
        }

        //valider le mdp
        if (motDePasse.length() < 6) {
            showToast("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        Map<String, Object> ud  = new HashMap<>();
        ud.put("email", courriel);
        ud.put("password", motDePasse);
        ud.put("last_name", nom);
        ud.put("first_name", prenom);
        ud.put("phone_number", "0123456789");
        ud.put("user_type", "client");
        ud.put("civic", streetNumber);
        ud.put("street", streetName);
        ud.put("city", city);
        ud.put("postal_code", postalCode);

        try {
            int civicNumber = Integer.parseInt(streetNumber);
            ud.put("civic", civicNumber);
        } catch(NumberFormatException e) {
            showToast("Numéro civique invalide");
            return;
        }


        //convertir JSON en Jackson
        String jsonString;
        try{
            jsonString = mapper.writeValueAsString(ud);
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(RegisterClientActivity.this, "Erreur lors de la conversion Jackson.",  Toast.LENGTH_SHORT).show();
            return;
        }

        //cree le RequestBODY avec JSON string
        RequestBody bd = RequestBody.create(JSON, jsonString);

        //Creer le OkHttp request avec rqbody
        Request rq = new Request.Builder()
                .url("http://4.172.252.189:5000/createUser")
                .post(bd)
                .build();

        //eenqueue
        okHttpClient.newCall(rq).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("ERREUR: reseau");
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String responseBody = response.body() != null ? response.body().string() : "null";
                Log.d("RegisterClientActivity", "Response: " + responseBody);
                if (response.isSuccessful()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Compte Client cree aveec succes!");
                            navigateToLogin();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("ERRUER: email deja utilise ou donnees invalides!");
                        }
                    });

                    response.close();
                }
            }
        });



    }

    private void navigateToLogin() {
        Intent i = new Intent (RegisterClientActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }


        private void showToast (String message){
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

