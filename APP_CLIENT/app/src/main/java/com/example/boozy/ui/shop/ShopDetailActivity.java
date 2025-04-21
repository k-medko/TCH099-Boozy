package com.example.boozy.ui.shop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.CategoryAdapter;
import com.example.boozy.adapter.ProductAdapter;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.AvailabilityResponse;
import com.example.boozy.data.model.Produit;
import com.example.boozy.ui.client.ClientHomeActivity;
import com.example.boozy.ui.client.PaiementActivity;
import com.example.boozy.ui.client.ProfilClientActivity;
import com.example.boozy.ui.order.SuiviCommandeActivity;
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
    private List<Produit> produitList = new ArrayList<>();
    private List<String> categoryList = new ArrayList<>();
    private int storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_detail);

        setupFullScreen();
        initializeViews();

        String shopName = getIntent().getStringExtra("shopName");
        storeId = getIntent().getIntExtra("storeId", -1);
        displayShopName(shopName);

        configureRecyclerViews();
        setupBottomNavigation();

        if (storeId != -1) {
            fetchProductsFromAPI(storeId);
        }
    }

    private void setupFullScreen() {
        Window window = getWindow();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
    }

    private void initializeViews() {
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
    }

    private void displayShopName(String shopName) {
        TextView shopNameTextView = findViewById(R.id.magasinNomText);
        if (shopName != null) {
            shopNameTextView.setText(shopName);
        }
    }

    private void configureRecyclerViews() {
        productsRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        productAdapter = new ProductAdapter(produitList);
        productsRecyclerView.setAdapter(productAdapter);

        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categoryList, this::filterProducts);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void fetchProductsFromAPI(int shopId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<AvailabilityResponse>> call = apiService.getAvailabilityByShop(shopId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<AvailabilityResponse>> call, @NonNull Response<List<AvailabilityResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    produitList.clear();

                    for (AvailabilityResponse availability : response.body()) {
                        Produit produit = new Produit(
                                availability.getProductId(),
                                availability.getProduct().getName(),
                                availability.getProduct().getDescription(),
                                availability.getProduct().getPrice(),
                                availability.getProduct().getCategory(),
                                String.valueOf(availability.getShopId())
                        );
                        produit.setStock(availability.getQuantity());
                        produit.setQuantity(1); // utilisé pour la gestion de panier
                        produit.setImageName(availability.getProduct().getImage()); // si image utilisée

                        produitList.add(produit);
                    }

                    productAdapter.notifyDataSetChanged();
                    updateCategoriesFromProducts();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AvailabilityResponse>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void updateCategoriesFromProducts() {
        categoryList.clear();
        List<String> unique = new ArrayList<>();

        for (Produit produit : produitList) {
            String category = produit.getCategory();
            if (category != null && !unique.contains(category)) {
                unique.add(category);
            }
        }

        categoryList.add("Tous");
        categoryList.addAll(unique);
        categoryAdapter.notifyDataSetChanged();
    }

    private void filterProducts(String category) {
        if (category.equals("Tous")) {
            productAdapter.updateProductList(produitList);
        } else {
            List<Produit> filteredList = new ArrayList<>();
            for (Produit produit : produitList) {
                if (produit.getCategory().equalsIgnoreCase(category)) {
                    filteredList.add(produit);
                }
            }
            productAdapter.updateProductList(filteredList);
        }
    }

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
            } else if (id == R.id.nav_profile) {
                navigateToProfil();
                return true;
            } else if (id == R.id.nav_notifications) {
                navigateToOrder();
                return true;
            }
            return false;
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, ClientHomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToCart() {
        Intent intent = new Intent(this, PaiementActivity.class);
        startActivity(intent);
    }

    private void navigateToProfil() {
        Intent intent = new Intent(this, ProfilClientActivity.class);
        startActivity(intent);
    }

    private void navigateToOrder() {
        Intent intent = new Intent(this, SuiviCommandeActivity.class);
        startActivity(intent);
    }
}
