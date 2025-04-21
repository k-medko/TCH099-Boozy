package com.example.boozy.ui.order;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
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
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.delivery.LivreurHomeActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommandeEnCoursActivity extends AppCompatActivity {

    private TextView textNumeroCommande, textClient, textRecuperation, textDestination;
    private Button buttonDemarrer, buttonLivree;
    private ImageView buttonBack;
    private String adresseMagasin;
    private String adresseLivraison;
    private FusedLocationProviderClient fusedLocationClient;
    private ApiService api;
    private int orderId;
    private boolean shippingDejaFait = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commande_en_cours);

        setupFullScreen();
        initializeViews();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);

        orderId = getIntent().getIntExtra("orderId", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Commande invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        afficherCommandeDepuisExtras();

        buttonBack.setOnClickListener(v -> navigateBack());
        buttonDemarrer.setOnClickListener(v -> {
            if (!shippingDejaFait) {
                updateOrderStatus("Shipping");
            } else {
                showNavigationOptions();
            }
        });
        buttonLivree.setOnClickListener(v -> updateOrderStatus("Completed"));
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
        textNumeroCommande = findViewById(R.id.textNumeroCommande);
        textClient = findViewById(R.id.textClient);
        textRecuperation = findViewById(R.id.textRecuperation);
        textDestination = findViewById(R.id.textDestination);
        buttonDemarrer = findViewById(R.id.buttonDemarrer);
        buttonLivree = findViewById(R.id.buttonLivree);
        buttonBack = findViewById(R.id.buttonBack);
    }

    private void afficherCommandeDepuisExtras() {
        orderId = getIntent().getIntExtra("orderId", -1);
        String shopAddress = getIntent().getStringExtra("shopAddress");

        textNumeroCommande.setText("Commande #" + orderId);
        textClient.setText("Client : Information privée");
        textRecuperation.setText(shopAddress);
        textDestination.setText("Destination : sera disponible après Démarrer");

        adresseMagasin = shopAddress;
        adresseLivraison = "Adresse du client (à afficher après Shipping)";
    }

    private void updateOrderStatus(String status) {
        String email = UtilisateurManager.getInstance(this).getEmail();
        String password = UtilisateurManager.getInstance(this).getPassword();

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("status", status);

        api.updateOrder(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().get("status"))) {
                    Toast.makeText(CommandeEnCoursActivity.this, "Statut mis à jour : " + status, Toast.LENGTH_SHORT).show();

                    if ("Shipping".equals(status)) {
                        shippingDejaFait = true;
                        Map<String, Object> clientInfo = (Map<String, Object>) response.body().get("client_info");
                        if (clientInfo != null) {
                            String nomClient = clientInfo.get("first_name") + " " + clientInfo.get("last_name");
                            String adresseClient = (String) clientInfo.get("address");
                            textClient.setText("Client : " + nomClient);
                            textDestination.setText(adresseClient);
                            adresseLivraison = adresseClient;
                            textDestination.setOnClickListener(v -> showNavigationOptions());
                        }
                    }

                    if ("Completed".equals(status)) {
                        UtilisateurManager.getInstance(CommandeEnCoursActivity.this).clearCommandeActive();
                        navigateBack();
                    }

                } else {
                    Toast.makeText(CommandeEnCoursActivity.this,
                            "Échec de la mise à jour : " +
                                    (response.body() != null ? response.body().get("message") : "erreur inconnue"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(CommandeEnCoursActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNavigationOptions() {
        View view = LayoutInflater.from(this).inflate(R.layout.popup_itineraire, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        view.findViewById(R.id.buttonGoogleMaps).setOnClickListener(v -> {
            dialog.dismiss();
            openInGoogleMaps();
        });

        view.findViewById(R.id.buttonWaze).setOnClickListener(v -> {
            dialog.dismiss();
            openInWaze();
        });

        dialog.show();
    }

    private void openInGoogleMaps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setNumUpdates(1);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null || locationResult.getLocations().isEmpty()) {
                    Toast.makeText(CommandeEnCoursActivity.this, "Position actuelle introuvable", Toast.LENGTH_SHORT).show();
                    return;
                }

                Location location = locationResult.getLastLocation();
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                String origin = lat + "," + lon;
                String waypoint = Uri.encode(adresseMagasin);
                String destination = Uri.encode(adresseLivraison.replaceAll("(?i)apt\\s*\\d+", "").trim());

                String uri = "https://www.google.com/maps/dir/?api=1"
                        + "&origin=" + origin
                        + "&destination=" + destination
                        + "&waypoints=" + waypoint
                        + "&travelmode=driving";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }


    private void openInWaze() {
        try {
            String uri = "waze://?q=" + Uri.encode(adresseLivraison);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Waze non installé", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBack() {
        Intent intent = new Intent(this, LivreurHomeActivity.class);
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
}
