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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

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

public class RegisterDeliveryActivity extends AppCompatActivity {

    private EditText inputName, inputFirstName, inputEmail, inputPhone, inputDrivingLicense, inputPassword;
    private Button buttonCreateAccountDelivery;

    private OkHttpClient okHttpClient;
    private ObjectMapper mapper;

    private static final MediaType  JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_register_delivery);
        initializeViews();

        okHttpClient  = new OkHttpClient();
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
        inputName = findViewById(R.id.inputName);
        inputFirstName = findViewById(R.id.inputFirstName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputDrivingLicense = findViewById(R.id.inputDrivingLicense);
        inputPassword = findViewById(R.id.inputPassword);
        buttonCreateAccountDelivery = findViewById(R.id.buttonCreateAccountDelivery);

        buttonCreateAccountDelivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });
    }

    private void handleRegistration() {
        String nom = inputName.getText().toString().trim();
        String prenom = inputFirstName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String tel = inputPhone.getText().toString().trim();
        String plaque = inputDrivingLicense.getText().toString().trim();
        String mdp = inputPassword.getText().toString().trim();

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || tel.isEmpty() || plaque.isEmpty() || mdp.isEmpty()) {
            showToast("Veuillez remplir tous les champs");
            return;
        }

        if (mdp.length() < 6) {
            showToast("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("password", mdp);
        map.put("last_name", nom);
        map.put("first_name", prenom);
        map.put("phone_number", tel);
        map.put("user_type", "carrier");
        map.put("license_plate", plaque);


        String js;
        try{
            js = mapper.writeValueAsString(map);
        }catch (IOException e){
            e.printStackTrace();
            showToast("Erreur lors de la conversion Jackson");
            return;
        }

        RequestBody body = RequestBody.create(JSON,js);

        Request  rq = new Request.Builder()
                .url("http://4.172.252.189:5000/createUser")
                .post(body)
                .build();

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

                if(response.isSuccessful()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("SUCCES : Creation du livreur");
                            navigateToLogin();
                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Erreur: email deja utilise ou donnees invalides");
                        }
                    });
                }
                response.close();
            }
        });
    }

    private void navigateToLogin() {
        showToast("Compte créé avec succès. Veuillez vous connecter.");
        Intent intent = new Intent(RegisterDeliveryActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
