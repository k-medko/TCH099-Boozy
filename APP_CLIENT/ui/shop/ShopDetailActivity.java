package com.example.boozy.ui.shop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.ProductAdapter;
import com.example.boozy.adapter.CategoryAdapter;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.Produit;
import com.example.boozy.data.model.ShopProduct;
import com.example.boozy.ui.client.ClientHomeActivity;
import com.example.boozy.ui.client.PaiementActivity;
import com.example.boozy.utils.GridSpacingItemDecoration;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ShopDetailActivity extends AppCompatActivity {

    private RecyclerView productsRecyclerView, categoryRecyclerView;
    private ProductAdapter productAdapter;
    private CategoryAdapter categoryAdapter;
    private List<Produit> produitList;
    private List<String> categoryList;

    // Adresse de base de votre serveur Flask
    private static final String BASE_URL = "http://4.172.252.189:5000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        setupFullScreen();
        initializeViews();

        // Récupération du nom du magasin et de l'ID passés via l'Intent
        String shopName = getIntent().getStringExtra("shopName");
        int storeId = getIntent().getIntExtra("storeId", -1);

        displayShopName(shopName);

        if (storeId != -1) {
            fetchProductsFromAPI(storeId);
            fetchCategoriesFromAPI(storeId);
        }

        configureRecyclerViews();
        setupBottomNavigation();
    }

    // ----------------------- Configuration de l'interface utilisateur -----------------------

    /**
     * Applique un effet plein écran en masquant partiellement la barre système.
     */
    private void setupFullScreen() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    /**
     * Initialise les vues de l'activité.
     */
    private void initializeViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
    }

    /**
     * Affiche le nom du magasin dans le TextView dédié.
     */
    private void displayShopName(String shopName) {
        TextView shopNameTextView = findViewById(R.id.magasinNomText);
        if (shopName != null) {
            shopNameTextView.setText(shopName);
        }
    }

    /**
     * Configure les RecyclerViews pour afficher les produits et les catégories.
     */
    private void configureRecyclerViews() {
        // Configuration pour les produits en grille de 2 colonnes
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        int spacing = 14;
        productsRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        // Configuration pour l'affichage horizontal des catégories
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialisation des listes
        produitList = new ArrayList<>();
        categoryList = new ArrayList<>();

        // Adaptateur pour les catégories avec filtrage
        categoryAdapter = new CategoryAdapter(categoryList, category -> filterProducts(category));
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Adaptateur pour les produits
        productAdapter = new ProductAdapter(produitList);
        productsRecyclerView.setAdapter(productAdapter);
    }

    // ----------------------- Appels API avec Retrofit -----------------------

    /**
     * Récupère les produits disponibles dans le magasin (endpoint /getAvailability).
     * Chaque ShopProduct contient un objet 'produit' avec les détails.
     *
     * @param storeId L'identifiant du magasin.
     */
    private void fetchProductsFromAPI(int storeId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<ShopProduct>> call = apiService.getAvailability(storeId, null, null);

        call.enqueue(new Callback<List<ShopProduct>>() {
            @Override
            public void onResponse(@NonNull Call<List<ShopProduct>> call, @NonNull Response<List<ShopProduct>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    produitList.clear();

                    for (ShopProduct shopProduct : response.body()) {
                        // Récupération des données de base
                        int idProduit = shopProduct.getProductId();
                        int quantity = shopProduct.getQuantity();

                        // Récupération des informations du produit depuis l'objet imbriqué
                        String name = shopProduct.getProduit().getName();
                        String description = "";
                        double price = shopProduct.getProduit().getPrice();
                        String category = shopProduct.getProduit().getCategory();
                        double alcohol = shopProduct.getProduit().getAlcohol();


                        String imageName = "placeholder";

                        Produit produit = new Produit(idProduit, name, description, price, category, alcohol, quantity, imageName);
                        produitList.add(produit);
                    }
                    productAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ShopProduct>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    /**
     * Récupère les catégories uniques depuis les produits du magasin en appelant le même endpoint.
     *
     * @param storeId L'identifiant du magasin.
     */
    private void fetchCategoriesFromAPI(int storeId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<ShopProduct>> call = apiService.getAvailability(storeId, null, null);

        call.enqueue(new Callback<List<ShopProduct>>() {
            @Override
            public void onResponse(@NonNull Call<List<ShopProduct>> call, @NonNull Response<List<ShopProduct>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ShopProduct> shopProducts = response.body();
                    categoryList.clear();
                    List<String> uniqueCategories = new ArrayList<>();

                    for (ShopProduct shopProduct : shopProducts) {
                        String cat = shopProduct.getProduit().getCategory();
                        if (cat != null && !uniqueCategories.contains(cat)) {
                            uniqueCategories.add(cat);
                        }
                    }
                    // Ajoute "Tous" pour afficher toutes les catégories
                    categoryList.add("Tous");
                    categoryList.addAll(uniqueCategories);
                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ShopProduct>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // ----------------------- Filtrage des produits -----------------------

    /**
     * Filtre la liste des produits selon la catégorie sélectionnée.
     *
     * @param category La catégorie de filtrage.
     */
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

    // ----------------------- Navigation -----------------------

    /**
     * Configure la barre de navigation inférieure.
     */
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

    /**
     * Navigue vers l'activité ClientHomeActivity.
     */
    private void navigateToHome() {
        Intent intent = new Intent(ShopDetailActivity.this, ClientHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navigue vers l'activité PaiementActivity.
     */
    private void navigateToCart() {
        Intent intent = new Intent(ShopDetailActivity.this, PaiementActivity.class);
        startActivity(intent);
    }
}
