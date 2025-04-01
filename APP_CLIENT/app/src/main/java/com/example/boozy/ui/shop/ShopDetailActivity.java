package com.example.boozy.ui.shop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.ProductAdapter;
import com.example.boozy.adapter.CategoryAdapter;
import com.example.boozy.data.model.Produit;
import com.example.boozy.ui.client.ClientHomeActivity;
import com.example.boozy.ui.client.PaiementActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ShopDetailActivity extends AppCompatActivity {

    private RecyclerView productsRecyclerView, categoryRecyclerView;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Produit> produitList;
    private List<String> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        // Effet plein écran
        setupFullScreen();

        // Initialisation des vues
        initializeViews();

        // Récupération du nom du magasin
        String shopName = getIntent().getStringExtra("shopName");
        displayShopName(shopName);

        // Configuration des RecyclerViews
        configureRecyclerViews();

        // Initialisation des catégories et produits
        fetchCategoriesFromAPI();
        fetchProductsFromAPI();

        // Gestion de la barre de navigation inférieure
        setupBottomNavigation();
    }

    // Méthode pour configurer l'affichage plein écran
    private void setupFullScreen() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(getResources().getColor(R.color.brown));
    }

    // Initialisation des vues
    private void initializeViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
    }

    // Affichage du nom du magasin
    private void displayShopName(String shopName) {
        TextView shopNameTextView = findViewById(R.id.magasinNomText);
        if (shopName != null && shopName.startsWith("SAQ ")) {
            String[] parts = shopName.split(" ", 2);
            shopNameTextView.setText(parts.length > 1 ? parts[0] + "\n" + parts[1] : shopName);
        } else {
            shopNameTextView.setText(shopName);
        }
    }

    // Configuration des RecyclerViews pour produits et catégories
    private void configureRecyclerViews() {
        // Configuration RecyclerView produits
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // Configuration RecyclerView catégories
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialisation des listes de produits et catégories
        produitList = new ArrayList<>();
        categoryList = new ArrayList<>();

        // Initialisation des adaptateurs
        categoryAdapter = new CategoryAdapter(categoryList, this::filterProducts);
        productsRecyclerView.setAdapter(productAdapter = new ProductAdapter(produitList));

        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    // Récupérer les catégories depuis l'API (à compléter)
    private void fetchCategoriesFromAPI() {
        // Appel API pour récupérer les catégories et mettre à jour 'categoryList'

    }

    // Récupérer les produits depuis l'API (à compléter)
    private void fetchProductsFromAPI() {
        // Appel API pour récupérer les produits et remplir 'produitList'
    }

    // Filtrer les produits par catégorie
    private void filterProducts(String category) {
        if (category.equals("Tous")) {
            productAdapter.updateProductList(produitList);
        } else {
            List<Produit> filteredList = new ArrayList<>();
            for (Produit produit : produitList) {
                if (produit.getCategory().equals(category)) {
                    filteredList.add(produit);
                }
            }
            productAdapter.updateProductList(filteredList);
        }
    }

    // Configuration de la barre de navigation inférieure
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                navigateToHome();
                return true;
            } else if (id == R.id.nav_orders) {
                navigateToCart();
                return true;
            }
            return false;
        });
    }

    // Naviguer vers la page d'accueil
    private void navigateToHome() {
        Intent intent = new Intent(ShopDetailActivity.this, ClientHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Naviguer vers la page du panier
    private void navigateToCart() {
        Intent intent = new Intent(ShopDetailActivity.this, PaiementActivity.class);
        startActivity(intent);
    }
}
