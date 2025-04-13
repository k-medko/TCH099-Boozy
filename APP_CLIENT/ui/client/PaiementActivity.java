package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.PanierAdapter;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.AddressLine;
import com.example.boozy.data.model.ClientOrder;
import com.example.boozy.data.model.CreateOrderResponse;
import com.example.boozy.data.model.PanierManager;
import com.example.boozy.data.model.PaymentOrder;
import com.example.boozy.data.model.Produit;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.order.SuiviCommandeActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * PaiementActivity
 * -----------------
 * Cette activité permet au client de consulter son panier, de calculer les totaux (sous-total, taxes, total),
 * de saisir les informations de paiement via des EditText, et de passer une commande.
 * En cas de succès, l'utilisateur est redirigé vers l'écran de suivi de commande.
 */
public class PaiementActivity extends AppCompatActivity {

    // Vues d'interface
    private RecyclerView recyclerViewCommande;
    private PanierAdapter panierAdapter;
    private TextView sousTotalText, taxesText, totalText, adresseText, carteText;
    private Button buttonPlaceOrder;

    // EditText pour la saisie des informations de paiement
    private EditText etCardName, etCardNumber, etExpiryMonth, etExpiryYear, etCvc;

    // Montant total calculé
    private double computedTotal = 0.0;
    // Adresse utilisateur récupérée localement
    private AddressLine userAddress;

    // URL de base du serveur (rien à changer côté serveur)
    private static final String BASE_URL = "http://4.172.252.189:5000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paiement);

        setupFullScreen();
        initializeViews();

        // Initialisation du module de paiement (ici, on affiche un message invitant à saisir la carte)
        initPaiement();

        // Récupère l'adresse de l'utilisateur depuis le Gestionnaire Utilisateur (méthode getAdresse() doit être implémentée)
        int userId = UtilisateurManager.getInstance(getApplicationContext()).getId();
        fetchUserAddress(userId);

        // Recharge le panier si l'activité est rafraîchie
        if (getIntent().getBooleanExtra("refresh", false)) {
            initPanier();
        }

        ImageButton backBtn = findViewById(R.id.buttonBack);
        backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    /**
     * Configure l'activité pour un affichage en plein écran avec barre de navigation transparente.
     */
    private void setupFullScreen() {
        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    /**
     * Lie les vues XML aux variables Java, y compris les EditText pour les infos de carte.
     */
    private void initializeViews() {
        recyclerViewCommande = findViewById(R.id.recyclerViewCommande);
        sousTotalText = findViewById(R.id.sousTotalText);
        taxesText = findViewById(R.id.taxesText);
        totalText = findViewById(R.id.totalText);
        adresseText = findViewById(R.id.adresseText);
        carteText = findViewById(R.id.carteText);
        buttonPlaceOrder = findViewById(R.id.buttonPlaceOrder);

        // Ces EditText doivent être présents dans votre layout activity_paiement.xml
        etCardName = findViewById(R.id.etCardName);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiryMonth = findViewById(R.id.etExpiryMonth);
        etExpiryYear = findViewById(R.id.etExpiryYear);
        etCvc = findViewById(R.id.etCvc);
    }

    /**
     * Calcule et affiche les totaux du panier (sous-total, taxes, total).
     *
     * @param panier La liste des produits du panier.
     */
    private void updateTotals(List<Produit> panier) {
        double sousTotal = 0;
        for (Produit p : panier) {
            sousTotal += p.getPrice() * p.getQuantity();
        }
        double taxes = sousTotal * 0.15;
        double total = sousTotal + taxes;
        computedTotal = total;
        sousTotalText.setText(String.format("$%.2f", sousTotal));
        taxesText.setText(String.format("$%.2f", taxes));
        totalText.setText(String.format("$%.2f", total));
    }

    /**
     * Initialise le panier local en chargeant les produits depuis PanierManager.
     */
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

    /**
     * Met à jour le panier via PanierManager.
     *
     * @param updatedList La nouvelle liste de produits.
     */
    private void updateCart(List<Produit> updatedList) {
        PanierManager.getInstance(getApplicationContext()).clearCart();
        for (Produit produit : updatedList) {
            PanierManager.getInstance(getApplicationContext()).addProduct(produit);
        }
        panierAdapter.updateProductList(updatedList);
        panierAdapter.notifyDataSetChanged();
        updateTotals(updatedList);
    }

    /**
     * Récupère l'adresse de l'utilisateur via UtilisateurManager.
     *
     * @param userId L'identifiant de l'utilisateur.
     */
    private void fetchUserAddress(int userId) {
        // On suppose que UtilisateurManager a une méthode getAdresse() qui retourne un objet AddressLine
        AddressLine adresse = UtilisateurManager.getInstance(getApplicationContext()).getAdresse();
        if (adresse != null) {
            userAddress = adresse;
            String adresseComplete = adresse.getCivic() + " " +
                    adresse.getStreet() + ", " +
                    adresse.getCity() + ", " +
                    adresse.getPostalCode();
            adresseText.setText(adresseComplete);
        } else {
            adresseText.setText("Adresse non définie");
        }
    }

    /**
     * Initialise le module de paiement.
     * Ici, nous invitons l'utilisateur à saisir ses informations de carte via les EditText.
     */
    private void initPaiement() {
        carteText.setText("Veuillez saisir vos informations de carte ci-dessous");
        buttonPlaceOrder.setEnabled(true);
        buttonPlaceOrder.setBackgroundColor(getResources().getColor(R.color.brown));
        int userId = UtilisateurManager.getInstance(getApplicationContext()).getId();
        fetchUserAddress(userId);
    }

    /**
     * Renvoie la date et l'heure actuelles au format "yyyy-MM-dd HH:mm:ss".
     *
     * @return La date formatée.
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Passe la commande en créant un objet PaymentOrder incluant les informations de paiement saisies via les EditText,
     * et en envoyant cet objet via l'API /createOrder. En cas de succès, le panier est vidé et l'utilisateur est redirigé
     * vers l'activité SuiviCommandeActivity.
     */
    private void placeOrder() {
        List<Produit> panier = PanierManager.getInstance(getApplicationContext()).getCart();
        if (panier.isEmpty()) {
            Toast.makeText(this, "Votre panier est vide", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userAddress == null) {
            Toast.makeText(this, "Adresse utilisateur non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        // Récupération des informations de carte depuis les EditText
        String cardName = etCardName.getText().toString().trim();
        String cardNumber = etCardNumber.getText().toString().trim();
        String expiryMonthStr = etExpiryMonth.getText().toString().trim();
        String expiryYearStr = etExpiryYear.getText().toString().trim();
        String cvc = etCvc.getText().toString().trim();

        if (cardName.isEmpty() || cardNumber.isEmpty() || expiryMonthStr.isEmpty() ||
                expiryYearStr.isEmpty() || cvc.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir toutes les informations de carte", Toast.LENGTH_SHORT).show();
            return;
        }

        int expiryMonth, expiryYear;
        try {
            expiryMonth = Integer.parseInt(expiryMonthStr);
            expiryYear = Integer.parseInt(expiryYearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Les informations de date d'expiration sont invalides", Toast.LENGTH_SHORT).show();
            return;
        }

        // ShopId : à définir selon votre logique (ici valeur par défaut 1)
        int shopId = 1;
        int userId = UtilisateurManager.getInstance(getApplicationContext()).getId();

        // Construction de l'objet PaymentOrder
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setClientOrderId(0); // 0 pour que le serveur en génère un
        paymentOrder.setCreationDate(getCurrentTime());
        paymentOrder.setStatus("Searching");
        paymentOrder.setTotalAmount(computedTotal);
        paymentOrder.setAddressId(userAddress.getAddressId());
        paymentOrder.setShopId(shopId);
        paymentOrder.setClientId(userId);
        paymentOrder.setCarrierId(0); // Le serveur attribuera un livreur

        // Ajout des informations de paiement
        paymentOrder.setCardName(cardName);
        paymentOrder.setCardNumber(cardNumber);
        paymentOrder.setCvc(cvc);
        paymentOrder.setExpiryDateMonth(expiryMonth);
        paymentOrder.setExpiryDateYear(expiryYear);

        Toast.makeText(this, "Envoi de la commande...", Toast.LENGTH_SHORT).show();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<CreateOrderResponse> call = apiService.createOrder(paymentOrder);

        call.enqueue(new Callback<CreateOrderResponse>() {
            @Override
            public void onResponse(Call<CreateOrderResponse> call, Response<CreateOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        "success".equalsIgnoreCase(response.body().getStatus())) {
                    PanierManager.getInstance(getApplicationContext()).clearCart();
                    Intent intent = new Intent(PaiementActivity.this, SuiviCommandeActivity.class);
                    intent.putExtra("orderId", response.body().getOrderId());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PaiementActivity.this, "Erreur lors de la commande", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateOrderResponse> call, Throwable t) {
                Toast.makeText(PaiementActivity.this, "Erreur de connexion : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
