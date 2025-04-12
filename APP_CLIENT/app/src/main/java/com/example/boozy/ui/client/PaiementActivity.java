package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.PanierAdapter;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.Adresse;
import com.example.boozy.data.model.Commande;
import com.example.boozy.data.model.PanierManager;
import com.example.boozy.data.model.Produit;
import com.example.boozy.data.model.Utilisateur;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.order.SuiviCommandeActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaiementActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCommande;
    private PanierAdapter panierAdapter;
    private TextView sousTotalText, taxesText, totalText, adresseText, carteText;
    private Button buttonPlaceOrder;
    private String numeroCommande = "#CMD" + System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paiement);

        setupFullScreen();

        initializeViews();
        initPaiement();

        // Récupérer l'adresse de l'utilisateur connecté
        int userId = UtilisateurManager.getInstance(getApplicationContext()).getId();
        fetchUserAddress(userId);

        // Vérifier si l'activity est appelée après ajout au panier
        if (getIntent().getBooleanExtra("refresh", false)) {
            initPanier();
        }

        ImageButton backBtn = findViewById(R.id.buttonBack);
        backBtn.setOnClickListener(v -> onBackPressed());

        // Placer la commande
        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
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
        recyclerViewCommande = findViewById(R.id.recyclerViewCommande);
        sousTotalText = findViewById(R.id.sousTotalText);
        taxesText = findViewById(R.id.taxesText);
        totalText = findViewById(R.id.totalText);
        adresseText = findViewById(R.id.adresseText);
        carteText = findViewById(R.id.carteText);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);
    }

    // Redirection vers la page de profil
    private void navigateToProfil() {
        Intent intent = new Intent(PaiementActivity.this, ProfilClientActivity.class);
        startActivity(intent);
    }

    // Mise à jour des totaux
    private void updateTotals(List<Produit> panier) {
        double sousTotal = 0;
        for (Produit p : panier) {
            sousTotal += (p.getPrice()) * p.getQuantity();
        }

        double taxes = sousTotal * 0.15;
        double total = sousTotal + taxes;

        // Vérifier si le panier est vide pour réinitialiser les valeurs
        if (panier.isEmpty()) {
            sousTotal = 0;
            taxes = 0;
            total = 0;
        }

        sousTotalText.setText(String.format("$%.2f", sousTotal));
        taxesText.setText(String.format("$%.2f", taxes));
        totalText.setText(String.format("$%.2f", total));
    }

    // Initialisation du panier local
    private void initPanier() {
        // Récupérer les produits depuis PanierManager (local)
        List<Produit> panier = PanierManager.getInstance(getApplicationContext()).getCart();

        // Vérifier si le panier est vide
        if (panier.isEmpty()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            updateTotals(panier);
            return;
        }

        panierAdapter = new PanierAdapter(this, panier, updatedList -> {
            updateCart(updatedList);
            updateTotals(updatedList);
        });

        recyclerViewCommande.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewCommande.setAdapter(panierAdapter);

        panierAdapter.updateProductList(panier);
        updateTotals(panier);
    }

    private void updateCart(List<Produit> updatedList) {
        PanierManager.getInstance(getApplicationContext()).clearCart();
        for (Produit produit : updatedList) {
            PanierManager.getInstance(getApplicationContext()).addProduct(produit);
        }
        panierAdapter.updateProductList(updatedList);
        panierAdapter.notifyDataSetChanged();
        updateTotals(updatedList);
    }


    private void placeOrder() {
        // Vérifier si le panier est vide avant de passer la commande
        List<Produit> panier = PanierManager.getInstance(getApplicationContext()).getCart();
        if (panier.isEmpty()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        // APPEL À L'API ICI
        // Envoyer la commande (numéro, panier, total) au serveur via un appel POST.

        // Création de la commande
        Commande commande = new Commande(
                numeroCommande,
                null,
                null,
                totalText.getText().toString(),
                null
        );

        // Envoi de la commande au serveur (à implémenter)
        // Utiliser une requête POST pour envoyer la commande

        // Si l'envoi est réussi, vider le panier local
        PanierManager.getInstance(getApplicationContext()).clearCart();

        // Redirection vers l'écran de suivi de commande
        Intent intent = new Intent(PaiementActivity.this, SuiviCommandeActivity.class);
        intent.putExtra("commande", commande);
        startActivity(intent);
    }

    // Initialisation du paiement (vérification de la carte)
    private void initPaiement() {
        // Vérifier si une carte est enregistrée localement
        String stripeCard = UtilisateurManager.getInstance(getApplicationContext()).getCarteStripe();
        if (stripeCard != null && !stripeCard.isEmpty()) {
            carteText.setText(stripeCard);
            buttonPlaceOrder.setEnabled(true);
            buttonPlaceOrder.setBackgroundColor(getResources().getColor(R.color.brown));
        } else {
            carteText.setText("Ajouter votre carte Stripe");
            buttonPlaceOrder.setEnabled(false);
            buttonPlaceOrder.setBackgroundColor(Color.GRAY);
        }

        // Récupérer l'adresse de l'utilisateur connecté
        int userId = UtilisateurManager.getInstance(getApplicationContext()).getId();
        fetchUserAddress(userId);
    }

    private void fetchUserAddress(int userId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.255.120:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<Adresse> call = apiService.getUserAddress(userId);

        call.enqueue(new Callback<Adresse>() {
            @Override
            public void onResponse(Call<Adresse> call, Response<Adresse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Adresse adresse = response.body();
                    // Afficher l'adresse complète
                    String adresseComplete = adresse.getNumeroCivique() + " " + adresse.getRue() + ", "
                            + adresse.getVille() + ", " + adresse.getCodePostal();
                    adresseText.setText(adresseComplete);
                } else {
                    Toast.makeText(PaiementActivity.this, "Erreur : Impossible de récupérer l'adresse", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Adresse> call, Throwable t) {
                Toast.makeText(PaiementActivity.this, "Erreur de connexion : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
