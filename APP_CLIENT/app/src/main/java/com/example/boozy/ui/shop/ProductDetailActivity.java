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

import com.bumptech.glide.Glide;
import com.example.boozy.R;
import com.example.boozy.data.model.PanierManager;
import com.example.boozy.data.model.Produit;
import com.example.boozy.ui.client.PaiementActivity;

public class ProductDetailActivity extends AppCompatActivity {

    private int quantity = 1;

    private TextView quantityText, productNameText, productPriceText, productDescriptionText;
    private ImageView productImageView;

    private int productId;
    private String productName, productDescription, productImageName;
    private double productPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        setupFullScreen();

        initViews();
        getProductFromIntent();
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
        productImageView = findViewById(R.id.productImage);
        quantityText = findViewById(R.id.quantityText);
    }

    private void getProductFromIntent() {
        productId = getIntent().getIntExtra("product_id", 0);
        productName = getIntent().getStringExtra("product_name");
        productDescription = getIntent().getStringExtra("product_description");
        productPrice = getIntent().getDoubleExtra("product_price", 0.0);
        productImageName = getIntent().getStringExtra("product_image_name");
    }

    private void displayProductData() {
        productNameText.setText(productName);
        productPriceText.setText("Prix : $" + productPrice);
        productDescriptionText.setText(productDescription);

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
            quantity++;
            quantityText.setText(String.valueOf(quantity));
        });

        minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
            }
        });
    }

    private void setupAddToCart() {
        Button addToCartButton = findViewById(R.id.buttonAddToCart);
        addToCartButton.setOnClickListener(v -> {
            Produit produit = new Produit(productId, productName, productDescription, productPrice, productImageName);
            produit.setQuantity(quantity);
            PanierManager.getInstance(getApplicationContext()).addProduct(produit);

            Toast.makeText(this, "Ajouté au panier", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProductDetailActivity.this, PaiementActivity.class);
            intent.putExtra("refresh", true);
            startActivity(intent);
            finish();
        });
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());
    }
}
