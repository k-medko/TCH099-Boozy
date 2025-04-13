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
import com.example.boozy.data.model.Adresse;
import com.example.boozy.data.model.Commande;
import com.example.boozy.data.model.PanierManager;
import com.example.boozy.data.model.Produit;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.order.SuiviCommandeActivity;

import java.util.List;

public class PaiementActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCommande;
    private PanierAdapter panierAdapter;
    private TextView sousTotalText, taxesText, totalText, adresseText, carteText;
    private Button buttonPlaceOrder;
    private String numeroCommande = "#CMD" + System.currentTimeMillis();

    private ImageView arrowNext, arrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paiement);

        setupFullScreen();
        initializeViews();

        arrowNext.setOnClickListener(v -> {
            Intent intent = new Intent(PaiementActivity.this, ProfilClientActivity.class);
            startActivity(intent);
        });

        arrow.setOnClickListener(v -> {
            Intent intent = new Intent(PaiementActivity.this, ProfilClientActivity.class);
            startActivity(intent);
        });

        initPaiement();
        initAdresseLocale();

        if (getIntent().getBooleanExtra("refresh", false)) {
            initPanier();
        }

        ImageButton backBtn = findViewById(R.id.buttonBack);
        backBtn.setOnClickListener(v -> onBackPressed());

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
        arrowNext = findViewById(R.id.arrow_next);
        arrow = findViewById(R.id.arrow);

    }

    private void initPaiement() {
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
    }

    private void initAdresseLocale() {
        Adresse adresse = UtilisateurManager.getInstance(getApplicationContext()).getAdresse();
        if (adresse != null) {
            String adresseComplete = adresse.getCivic() + " " + adresse.getStreet() + ", "
                    + adresse.getCity() + ", " + adresse.getPostalCode();
            adresseText.setText(adresseComplete);
        } else {
            adresseText.setText("Adresse non disponible");
        }
    }

    private void initPanier() {
        List<Produit> panier = PanierManager.getInstance(getApplicationContext()).getCart();
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

    private void updateTotals(List<Produit> panier) {
        double sousTotal = 0;
        for (Produit p : panier) {
            sousTotal += (p.getPrice()) * p.getQuantity();
        }

        double taxes = sousTotal * 0.15;
        double total = sousTotal + taxes;

        if (panier.isEmpty()) {
            sousTotal = 0;
            taxes = 0;
            total = 0;
        }

        sousTotalText.setText(String.format("$%.2f", sousTotal));
        taxesText.setText(String.format("$%.2f", taxes));
        totalText.setText(String.format("$%.2f", total));
    }

    private void placeOrder() {
        List<Produit> panier = PanierManager.getInstance(getApplicationContext()).getCart();
        if (panier.isEmpty()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }

        Commande commande = new Commande(
                numeroCommande,
                null,
                null,
                totalText.getText().toString(),
                null
        );

        PanierManager.getInstance(getApplicationContext()).clearCart();

        Intent intent = new Intent(PaiementActivity.this, SuiviCommandeActivity.class);
        intent.putExtra("commande", commande);
        startActivity(intent);
    }
}
