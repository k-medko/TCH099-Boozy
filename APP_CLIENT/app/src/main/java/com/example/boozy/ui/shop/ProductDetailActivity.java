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

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.model.PanierManager;
import com.example.boozy.data.model.Produit;
import com.example.boozy.ui.client.PaiementActivity;

public class ProductDetailActivity extends AppCompatActivity {

    private int quantity = 1;
    private TextView quantityText;
    private int price = 1000;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        setupFullScreen();

        // Récupération des données du produit depuis l'intent
        int id = getIntent().getIntExtra("product_id", 0);
        String name = getIntent().getStringExtra("product_name");
        int imageResId = getIntent().getIntExtra("product_image", R.drawable.produit);

        // Appel API pour récupérer les informations du produit
        fetchProductDataFromAPI(id);

        // Initialisation des vues
        TextView productNameText = findViewById(R.id.productName);
        TextView productPriceText = findViewById(R.id.productPrice);
        TextView productDescriptionText = findViewById(R.id.productDescription);
        ImageView productImageView = findViewById(R.id.productImage);
        quantityText = findViewById(R.id.quantityText);
        ImageButton plusButton = findViewById(R.id.buttonPlus);
        ImageButton minusButton = findViewById(R.id.buttonMinus);
        Button addToCartButton = findViewById(R.id.buttonAddToCart);
        ImageButton backButton = findViewById(R.id.buttonBack);

        // Affichage des données du produit récupérées
        productNameText.setText(name);
        productPriceText.setText("Prix : $" + (price / 100.0));
        productDescriptionText.setText(description);
        productImageView.setImageResource(imageResId);
        quantityText.setText(String.valueOf(quantity));

        // Gestion de la quantité
        plusButton.setOnClickListener(v -> {
            quantity++;
            quantityText.setText(String.valueOf(quantity));
        });

        minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
            }
        });

        // Ajouter au panier
        addToCartButton.setOnClickListener(v -> {
            Produit produit = new Produit(id, name, price, imageResId, description);
            produit.setQuantity(quantity);
            PanierManager.getInstance(getApplicationContext()).addProduct(produit);

            Toast.makeText(this, "Ajouté au panier", Toast.LENGTH_SHORT).show();

            // Redirection vers la page de paiement
            Intent intent = new Intent(ProductDetailActivity.this, PaiementActivity.class);
            intent.putExtra("product_name", name);
            intent.putExtra("product_quantity", quantity);
            startActivity(intent);

            finish();
        });

        // Retour arrière
        backButton.setOnClickListener(v -> finish());
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

    // Méthode pour récupérer les données du produit depuis l'API
    private void fetchProductDataFromAPI(int productId) {
        // Appel API pour récupérer les informations du produit par ID (nom, prix, description, image ?).
    }
}
