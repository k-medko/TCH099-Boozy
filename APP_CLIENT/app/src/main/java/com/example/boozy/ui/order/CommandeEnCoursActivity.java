package com.example.boozy.ui.order;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.boozy.R;
import com.example.boozy.ui.delivery.LivreurHomeActivity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.net.URLEncoder;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommandeEnCoursActivity extends AppCompatActivity {

    //vues
    private TextView textNumeroCommande, textClient, textRecuperation, textDestination, textEta;
    private Button buttonDemarrer, buttonLivree;
    private ImageView buttonBack;
    private String adresseMagasin;
    private String adresseLivraison;

    //localisation
    private FusedLocationProviderClient fusedLocationClient;

    //Okhhtp + Jackson
    private OkHttpClient okHttpClient;
    private ObjectMapper mapper;

    //Google Directions
    private static final String GOOGLE_API_KEY = "AIzaSyD8XPxmyNpeUnJmuBqMZ0un-IXkD8X2Mng";
    private static final String DIRECTIONS_ENDPOINT = "https://maps.googleapis.com/maps/api/directions/json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commande_en_cours);

        setupFullScreen();
        initializeViews();

        okHttpClient = new OkHttpClient();
        mapper = new ObjectMapper();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Charger les informations de la commande depuis l'API
        loadCommandeDetails();

        // Bouton retour
        buttonBack.setOnClickListener(v -> navigateBack());

        // Bouton itinéraire
        buttonDemarrer.setOnClickListener(v -> openGoogleMaps());

        // Bouton commande livrée
        buttonLivree.setOnClickListener(v -> markAsDelivered());
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

    // Initialisation des vues
    private void initializeViews() {
        textNumeroCommande = findViewById(R.id.textNumeroCommande);
        textClient = findViewById(R.id.textClient);
        textRecuperation = findViewById(R.id.textRecuperation);
        textDestination = findViewById(R.id.textDestination);
        buttonDemarrer = findViewById(R.id.buttonDemarrer);
        buttonLivree = findViewById(R.id.buttonLivree);
        buttonBack = findViewById(R.id.buttonBack);

        textEta = findViewById(R.id.textEta);
    }

    /**
     * Charger les détails de la commande depuis l'API
     */
    private void loadCommandeDetails() {
        Intent i = getIntent();
        if (i == null) return;

        String numero = i.getStringExtra("numeroCommande");
        adresseMagasin = i.getStringExtra("magaisn");
        adresseLivraison = i.getStringExtra("adresse");
        String montant = i.getStringExtra("montant");

        textNumeroCommande.setText("Commande " + numero);
        textClient.setText("Montant : " + montant);
        textRecuperation.setText(adresseMagasin);
        textDestination.setText(adresseLivraison);
        textEta.setText("Calcul...");

        //calcul ETA des que la localisation est disponible
        requestEtaFromCurrentLocation();

    }

    /**
     * Demande la localisation et lance Google Directions
     */
    private void requestEtaFromCurrentLocation(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //Demande la Permission GPS
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if(location != null){
                        String origin = location.getLatitude() + "," + location.getLongitude();
                        fetchEta(origin, adresseLivraison);
                    }else {
                        showToast("Localisation indisponible");
                    }
                });
    }

    private void fetchEta(String originLatlon, String destinationAddr){
        try{
            String destination = URLEncoder.encode(destinationAddr, "UTF-8");

            String url = DIRECTIONS_ENDPOINT
                    + "?origin=" + originLatlon
                    + "&destination=" + destination
                    +"&key=" + GOOGLE_API_KEY;

            Request rq = new Request.Builder().url(url).build();

            okHttpClient.newCall(rq).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                    showToast("Impossible de calculer l'ETA");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    try{
                        if(response.isSuccessful()){
                            JsonNode root = mapper.readTree(response.body().string());
                            String etaText = parseDuration(root);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textEta.setText(etaText);
                                }
                            });
                        }else {
                            showToast("ERREUR GOOGLE API : " + response.code());
                        }
                    }finally {
                        response.close();
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String parseDuration(JsonNode root){
        JsonNode routes = root.path("routes");
        if(routes.isArray() && routes.size() > 0 ){
            JsonNode legs = routes.get(0).path("legs");
            if(legs.isArray() && legs.size() > 0 ) {
                return legs.get(0).path("duration").path("text").asText();
            }
        }
        return "-";
    }


    // Démarrer l'itinéraire avec Google Maps
    private void openGoogleMaps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                String uri = "https://www.google.com/maps/dir/?api=1"
                        + "&origin=" + lat + "," + lon
                        + "&waypoints=" + Uri.encode(adresseMagasin)
                        + "&destination=" + Uri.encode(adresseLivraison);

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Localisation introuvable", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Marquer la commande comme livrée et mettre à jour le statut sur le serveur
     */
    private void markAsDelivered() {
        // APPEL À L'API ICI
        // Mettre à jour le statut de la commande en tant que "Livrée" sur le serveur via un appel POST.

        Toast.makeText(this, "Commande livrée !", Toast.LENGTH_SHORT).show();
        navigateBack();
    }

    // Retour à la page précédente
    private void navigateBack() {
        Intent intent = new Intent(CommandeEnCoursActivity.this, LivreurHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission accordée", Toast.LENGTH_SHORT).show();
        }
    }

    private void showToast(String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CommandeEnCoursActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
