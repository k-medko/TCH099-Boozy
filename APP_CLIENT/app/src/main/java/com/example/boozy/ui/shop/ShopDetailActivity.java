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

import com.example.boozy.utils.GridSpacingItemDecoration;
import com.example.boozy.R;
import com.example.boozy.adapter.ProductAdapter;
import com.example.boozy.adapter.CategoryAdapter;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.Produit;
import com.example.boozy.ui.client.ClientHomeActivity;
import com.example.boozy.ui.client.PaiementActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        setupFullScreen();
        initializeViews();

        // Récupération du nom du magasin et de l'ID
        String shopName = getIntent().getStringExtra("shopName");
        int storeId = getIntent().getIntExtra("storeId", -1);

        displayShopName(shopName);

        // Vérifie si l'ID du magasin est valide
        if (storeId != -1) {
            fetchProductsFromAPI(storeId);
            fetchCategoriesFromAPI(); // Appel ici pour charger les catégories après les produits
        }

        // Configuration des RecyclerViews
        configureRecyclerViews();

        // Gestion de la barre de navigation inférieure
        setupBottomNavigation();
    }


    // Effet plein écran
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


    // Initialisation des vues
    private void initializeViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
    }

    // Affichage du nom du magasin sur une seule ligne
    private void displayShopName(String shopName) {
        TextView shopNameTextView = findViewById(R.id.magasinNomText);
        if (shopName != null) {
            shopNameTextView.setText(shopName);
        }
    }

    // Configuration des RecyclerViews pour produits et catégories
    private void configureRecyclerViews() {

        // Configuration RecyclerView produits
        RecyclerView recyclerView = findViewById(R.id.productsRecyclerView);
        int spacing = 14;
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, spacing, true));

        // Configuration RecyclerView catégories
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initialisation des listes de produits et catégories
        produitList = new ArrayList<>();
        categoryList = new ArrayList<>();

        // Initialisation des adaptateurs
        categoryAdapter = new CategoryAdapter(categoryList, category -> filterProducts(category));
        categoryRecyclerView.setAdapter(categoryAdapter);

        productsRecyclerView.setAdapter(productAdapter = new ProductAdapter(produitList));
    }

    // Récupérer les catégories depuis l'API
    private void fetchCategoriesFromAPI() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.255.120:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<List<Object>>> call = apiService.getProducts(1);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<List<Object>>> call, @NonNull Response<List<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryList.clear();
                    List<String> uniqueCategories = new ArrayList<>();

                    for (List<Object> productData : response.body()) {
                        try {
                            String category = (String) productData.get(4);
                            if (!uniqueCategories.contains(category)) {
                                uniqueCategories.add(category);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Ajouter "Tous" pour afficher tous les produits
                    categoryList.add(0, "Tous");
                    categoryList.addAll(uniqueCategories);

                    categoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<List<Object>>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    // Récupérer les produits depuis l'API
    private void fetchProductsFromAPI(int storeId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.255.120:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<List<Object>>> call = apiService.getProducts(storeId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<List<Object>>> call, Response<List<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    produitList.clear();

                    for (List<Object> productData : response.body()) {
                        try {
                            int idProduit = ((Double) productData.get(0)).intValue();
                            String name = (String) productData.get(1);
                            String description = (String) productData.get(2);
                            double price = Double.parseDouble((String) productData.get(3));
                            String category = (String) productData.get(4);
                            int quantity = ((Double) productData.get(5)).intValue();
                            String imageName = (String) productData.get(6);

                            Produit produit = new Produit(idProduit, name, description, price, category, quantity, imageName);
                            produitList.add(produit);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    productAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<List<Object>>> call, Throwable t) {
                t.printStackTrace();
            }
        });
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
