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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class CommandeEnCoursActivity extends AppCompatActivity {

    private TextView textNumeroCommande, textClient, textRecuperation, textDestination;
    private Button buttonDemarrer, buttonLivree;
    private ImageView buttonBack;
    private String adresseMagasin;
    private String adresseLivraison;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commande_en_cours);

        setupFullScreen();
        initializeViews();
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
    }

    /**
     * Charger les détails de la commande depuis l'API
     */
    private void loadCommandeDetails() {
        // APPEL À L'API ICI
        // Récupérer les informations de la commande depuis le serveur via un appel GET.
        // # de la commande, nom du client, adresse du client, adresse du magasin.

    }

    // Retour à la page précédente
    private void navigateBack() {
        Intent intent = new Intent(CommandeEnCoursActivity.this, LivreurHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
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
