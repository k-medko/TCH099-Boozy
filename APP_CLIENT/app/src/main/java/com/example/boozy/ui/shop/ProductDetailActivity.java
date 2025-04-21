package com.example.boozy.ui.shop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.boozy.R;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.AvailabilityResponse;
import com.example.boozy.data.model.PanierManager;
import com.example.boozy.data.model.Produit;
import com.example.boozy.ui.client.PaiementActivity;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductDetailActivity extends AppCompatActivity {

    private int quantity = 1;
    private ImageButton plusButton;
    private TextView quantityText, productNameText, productPriceText, productDescriptionText, productAlcoolText;
    private ImageView productImageView;
    private int productId;
    private String productName, productDescription, productImageName, shopId;
    private double productPrice;
    private int stockDispo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        setupFullScreen();

        initViews();
        getProductFromIntent();
        fetchProductDescription();
        displayProductData();
        setupQuantityButtons();
        setupAddToCart();
        setupBackButton();
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

    private void initViews() {
        productNameText = findViewById(R.id.productName);
        productPriceText = findViewById(R.id.productPrice);
        productDescriptionText = findViewById(R.id.productDescription);
        productAlcoolText = findViewById(R.id.productAlcool);
        productImageView = findViewById(R.id.productImage);
        quantityText = findViewById(R.id.quantityText);
        plusButton = findViewById(R.id.buttonPlus);

    }

    private void getProductFromIntent() {
        productId = getIntent().getIntExtra("product_id", 0);
        productName = getIntent().getStringExtra("product_name");
        productPrice = getIntent().getDoubleExtra("product_price", 0.0);
        productImageName = getIntent().getStringExtra("product_image_name");
        shopId = getIntent().getStringExtra("shop_id");
    }

    private void fetchProductDescription() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        Call<List<Produit>> call = api.getProductsById(productId);
        call.enqueue(new Callback<List<Produit>>() {
            @Override
            public void onResponse(@NonNull Call<List<Produit>> call, @NonNull Response<List<Produit>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Produit produit = response.body().get(0);
                    productDescription = produit.getDescription();
                    productDescriptionText.setText(productDescription);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Produit>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });

        Call<List<AvailabilityResponse>> stockCall = api.getAvailabilityByShop(Integer.parseInt(shopId));
        stockCall.enqueue(new Callback<List<AvailabilityResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<AvailabilityResponse>> call, @NonNull Response<List<AvailabilityResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (AvailabilityResponse item : response.body()) {
                        if (item.getProductId() == productId) {
                            stockDispo = item.getQuantity();
                            double alcohol = item.getProduct().getAlcohol();
                            productAlcoolText.setText("Taux d'alcool : " + alcohol + "%");
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AvailabilityResponse>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }


    private void displayProductData() {
        productNameText.setText(productName);
        productPriceText.setText(productPrice + "$ / unité");

        Glide.with(this)
                .load("http://4.172.252.189:5000/images/" + productImageName)
                .placeholder(R.drawable.produit)
                .into(productImageView);

        quantityText.setText(String.valueOf(quantity));
    }

    private void setupQuantityButtons() {
        ImageButton plusButton = findViewById(R.id.buttonPlus);
        ImageButton minusButton = findViewById(R.id.buttonMinus);

        plusButton.setOnClickListener(v -> {
            if (quantity < stockDispo) {
                quantity++;
                quantityText.setText(String.valueOf(quantity));
                if (quantity == stockDispo) {
                    plusButton.setEnabled(false);
                }
            }
        });
        minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
                if (!plusButton.isEnabled()) {
                    plusButton.setEnabled(true);
                }
            }
        });
    }

    private void setupAddToCart() {
        Button addToCartButton = findViewById(R.id.buttonAddToCart);
        addToCartButton.setOnClickListener(v -> {
            Produit produit = new Produit(productId, productName, productDescription, productPrice, null, shopId);
            produit.setQuantity(quantity);
            produit.setImageName(productImageName);

            PanierManager panierManager = PanierManager.getInstance(getApplicationContext());
            String currentShopId = panierManager.getCurrentShopId();

            if (currentShopId != null && !currentShopId.equals(shopId)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Changer de magasin ?")
                        .setMessage("L’ajout de ce produit videra le panier actuel du magasin précédent.\nSouhaitez-vous continuer ?")
                        .setPositiveButton("Oui", (dialog, which) -> {
                            panierManager.clearCart();
                            panierManager.setCurrentShopId(shopId);
                            panierManager.addProduct(produit);

                            Toast.makeText(this, "Produit ajouté au nouveau panier", Toast.LENGTH_SHORT).show();
                            goToPaiement();
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            } else {
                if (currentShopId == null) {
                    panierManager.setCurrentShopId(shopId);
                }

                int dejaDansPanier = panierManager.getCart().stream()
                        .filter(p -> p.getId() == productId)
                        .mapToInt(Produit::getQuantity)
                        .sum();

                int quantiteTotale = dejaDansPanier + quantity;

                if (quantiteTotale > stockDispo) {
                    Toast.makeText(this, "Stock insuffisant. Stock disponible : " + stockDispo, Toast.LENGTH_SHORT).show();
                    return;
                }

                panierManager.addProduct(produit);
                Toast.makeText(this, "Ajouté au panier", Toast.LENGTH_SHORT).show();
                goToPaiement();
            }
        });
    }

    private void goToPaiement() {
        Intent intent = new Intent(ProductDetailActivity.this, PaiementActivity.class);
        intent.putExtra("refresh", true);
        startActivity(intent);
        finish();
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());
    }
}
