package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.boozy.R;
import com.example.boozy.adapter.PanierAdapter;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.Adresse;
import com.example.boozy.data.model.AvailabilityResponse;
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

    private ImageView arrowNext, arrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paiement);

        setupFullScreen();
        initializeViews();

        arrowNext.setOnClickListener(v -> openProfil());
        arrow.setOnClickListener(v -> openProfil());

        initPaiement();
        initAdresseLocale();

        if (getIntent().getBooleanExtra("refresh", false)) {
            initPanier();
        }

        findViewById(R.id.buttonBack).setOnClickListener(v -> onBackPressed());
        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void openProfil() {
        Intent intent = new Intent(PaiementActivity.this, ProfilClientActivity.class);
        startActivityForResult(intent, 1234);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1234 && resultCode == RESULT_OK) {
            initAdresseLocale();
            initPaiement();
        }
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
            buttonPlaceOrder.setBackgroundColor(getResources().getColor(R.color.wine_dark));
        } else {
            carteText.setText("Ajouter votre carte");
            buttonPlaceOrder.setEnabled(false);
            buttonPlaceOrder.setBackgroundColor(Color.GRAY);
        }
    }

    private void initAdresseLocale() {
        Adresse adresse = UtilisateurManager.getInstance(getApplicationContext()).getAdresse();
        if (adresse != null) {
            String adresseComplete = "";
            if (!adresse.getApartment().isEmpty()) {
                adresseComplete += adresse.getApartment() + "-";
            }

            adresseComplete += adresse.getCivic() + " " + adresse.getStreet() + ", "
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

        int shopId = Integer.parseInt(panier.get(0).getShopId());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService api = retrofit.create(ApiService.class);

        api.getAvailabilityByShop(shopId).enqueue(new Callback<List<AvailabilityResponse>>() {
            @Override
            public void onResponse(Call<List<AvailabilityResponse>> call, Response<List<AvailabilityResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (Produit p : panier) {
                        for (AvailabilityResponse stockItem : response.body()) {
                            if (stockItem.getProductId() == p.getId()) {
                                p.setStock(stockItem.getQuantity());
                                break;
                            }
                        }
                    }
                    setupAdapter(panier);
                } else {
                    Toast.makeText(PaiementActivity.this, "Erreur lors de la vérification du stock", Toast.LENGTH_SHORT).show();
                    setupAdapter(panier);
                }
            }

            @Override
            public void onFailure(Call<List<AvailabilityResponse>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(PaiementActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                setupAdapter(panier);
            }
        });
    }

    private void setupAdapter(List<Produit> panier) {
        panierAdapter = new PanierAdapter(this, panier, updatedList -> {
            updateCart(updatedList);
            updateTotals(updatedList);
        });

        recyclerViewCommande.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCommande.setAdapter(panierAdapter);
        panierAdapter.updateProductList(panier);
        updateTotals(panier);
    }


    private void updateCart(List<Produit> updatedList) {
        PanierManager panierManager = PanierManager.getInstance(getApplicationContext());
        panierManager.clearCart();

        if (!updatedList.isEmpty()) {
            String shopId = updatedList.get(0).getShopId();
            panierManager.setCurrentShopId(shopId);

            for (Produit produit : updatedList) {
                panierManager.addProduct(produit);
            }
        }

        panierAdapter.updateProductList(panierManager.getCart());
        panierAdapter.notifyDataSetChanged();
        updateTotals(panierManager.getCart());
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

        String email = UtilisateurManager.getInstance(this).getEmail();
        String password = UtilisateurManager.getInstance(this).getPassword();
        int shopId = Integer.parseInt(panier.get(0).getShopId());

        ArrayList<Map<String, Object>> items = new ArrayList<>();
        for (Produit produit : panier) {
            Map<String, Object> item = new HashMap<>();
            item.put("product_id", produit.getId());
            item.put("quantity", produit.getQuantity());
            items.add(item);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("shop_id", shopId);
        body.put("items", items);
        body.put("payment_method", "credit_card");
        body.put("card_name", "John Doe");
        body.put("card_number", "4111111111111111");
        body.put("CVC_card", "123");
        body.put("expiry_date_month", 5);
        body.put("expiry_date_year", 2026);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);
        Call<Map<String, Object>> call = api.createOrder(body);

        call.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    PanierManager.getInstance(getApplicationContext()).clearCart();
                    Toast.makeText(PaiementActivity.this, "Commande envoyée avec succès", Toast.LENGTH_SHORT).show();

                    Map<String, String> credentials = new HashMap<>();
                    credentials.put("email", email);
                    credentials.put("password", password);

                    Call<Map<String, Object>> getOrdersCall = api.getUserOrders(credentials);
                    getOrdersCall.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Map<String, Object>> orders = (List<Map<String, Object>>) response.body().get("orders");

                                if (!orders.isEmpty()) {
                                    int latestOrderId = ((Double) orders.get(0).get("order_id")).intValue();

                                    Intent intent = new Intent(PaiementActivity.this, SuiviCommandeActivity.class);
                                    intent.putExtra("order_id", latestOrderId);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(PaiementActivity.this, "Commande sans numéro", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(PaiementActivity.this, "Erreur récupération commande", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(PaiementActivity.this, "Erreur réseau post-commande", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    Toast.makeText(PaiementActivity.this, "Erreur lors de la commande", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(PaiementActivity.this, "Échec de connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
