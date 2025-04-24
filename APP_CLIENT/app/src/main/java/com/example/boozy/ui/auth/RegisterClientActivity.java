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
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Arrays;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RegisterClientActivity extends AppCompatActivity {

    private EditText inputFirstName, inputLastName, inputStreetNumber, inputStreetName;
    private EditText inputAppartment, inputPostalCode, inputCity, inputEmail, inputPassword;
    private EditText inputAdresseGoogle;
    private CheckBox checkboxAge;
    private Button buttonCreateAccount;

    private static final String GOOGLE_API_KEY = "AIzaSyBejKgvwIR3_s4HHopHGu8ZPzg1jssanJ8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_register_client);

        initializeViews();
        setupGooglePlacesAutocomplete();
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
        inputAdresseGoogle = findViewById(R.id.inputAdresseGoogle);

        buttonCreateAccount.setOnClickListener(v -> handleRegistration());
    }

    private void setupGooglePlacesAutocomplete() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), GOOGLE_API_KEY, Locale.FRENCH);
        }

        inputAdresseGoogle.setFocusable(false);
        inputAdresseGoogle.setHint("Rechercher une adresse");
        inputAdresseGoogle.setOnClickListener(v -> {
            com.google.android.libraries.places.api.model.RectangularBounds bounds =
                    com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
                            new com.google.android.gms.maps.model.LatLng(45.4100, -73.9500),
                            new com.google.android.gms.maps.model.LatLng(45.7000, -73.5000)
                    );

            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY,
                    Arrays.asList(Place.Field.ADDRESS_COMPONENTS, Place.Field.ADDRESS))
                    .setCountry("CA")
                    .setLocationBias(bounds)
                    .build(this);

            startActivityForResult(intent, 1002);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1002 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            inputAdresseGoogle.setText(place.getAddress());

            for (AddressComponent component : place.getAddressComponents().asList()) {
                if (component.getTypes().contains("street_number")) {
                    inputStreetNumber.setText(component.getName());
                } else if (component.getTypes().contains("route")) {
                    inputStreetName.setText(component.getName());
                } else if (component.getTypes().contains("locality")) {
                    inputCity.setText(component.getName());
                } else if (component.getTypes().contains("postal_code")) {
                    inputPostalCode.setText(component.getName());
                }
            }

            inputAdresseGoogle.setText("");

        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(this, "Erreur : " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
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

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(courriel);
        utilisateur.setPassword(motDePasse);
        utilisateur.setNumTel("5140000000");
        utilisateur.setTypeUtilisateur("client");
        utilisateur.setAdresse(adresse);

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Void> call = apiService.createUser(utilisateur);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    navigateToLogin();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Réponse vide";
                        Log.e("REGISTER_CLIENT", "Erreur " + response.code() + ": " + error);
                        showToast("Erreur : " + response.code());
                    } catch (Exception e) {
                        showToast("Erreur inconnue");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Erreur réseau : " + t.getMessage());
                Log.e("REGISTER_CLIENT", "Erreur réseau : ", t);
            }
        });
    }

    private void navigateToLogin() {
        showToast("Compte client créé avec succès.");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}