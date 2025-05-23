package com.example.boozy.ui.shop;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.client.ClientHomeActivity;
import com.example.boozy.ui.client.PaiementActivity;
import com.example.boozy.ui.client.ProfilClientActivity;
import com.example.boozy.ui.order.SuiviCommandeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                        produit.setImageName(String.valueOf(availability.getProductId()));
                        produit.setStock(availability.getQuantity());
                        produit.setQuantity(1);
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
        intent.putExtra("refresh", true);
        startActivity(intent);
    }

    private void navigateToProfil() {
        Intent intent = new Intent(this, ProfilClientActivity.class);
        startActivity(intent);
    }

    private void navigateToOrder() {
        String email = UtilisateurManager.getInstance(this).getEmail();
        String password = UtilisateurManager.getInstance(this).getPassword();

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        api.getUserOrders(credentials).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> orders = (List<Map<String, Object>>) response.body().get("orders");

                    if (orders != null && !orders.isEmpty()) {
                        orders.sort((o1, o2) -> {
                            String d1 = (String) o1.get("creation_date");
                            String d2 = (String) o2.get("creation_date");
                            return d2.compareTo(d1);
                        });

                        Map<String, Object> latestOrder = orders.get(0);
                        int latestOrderId = ((Double) latestOrder.get("order_id")).intValue();

                        Intent intent = new Intent(ShopDetailActivity.this, SuiviCommandeActivity.class);
                        intent.putExtra("order_id", latestOrderId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(ShopDetailActivity.this, "Aucune commande trouvée", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShopDetailActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(ShopDetailActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
