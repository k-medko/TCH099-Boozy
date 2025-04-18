package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.data.model.Shop;
import com.example.boozy.adapter.ShopAdapter;
import com.example.boozy.ui.shop.ShopDetailActivity;
import com.example.boozy.data.api.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ClientHomeActivity
 * ------------------
 * Affiche la liste des magasins pour le client.
 * Récupère les données à partir de l'API et met à jour le RecyclerView.
 */
public class ClientHomeActivity extends AppCompatActivity {

    private RecyclerView shopRecyclerView;
    private ShopAdapter shopAdapter;
    private List<Shop> shopList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen(); // configure l'affichage plein écran
        setContentView(R.layout.activity_client_home);

        initializeRecyclerView();
        fetchMagasins(); // lance la récupération des magasins via l'API
    }

    /**
     * Configure l'affichage en plein écran avec barres de statut et de navigation transparentes.
     */
    private void setFullScreen() {
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

    /**
     * Initialise le RecyclerView et l'adaptateur pour afficher les magasins.
     */
    private void initializeRecyclerView() {
        shopRecyclerView = findViewById(R.id.shopRecyclerView);
        shopList = new ArrayList<>();
        // Ici, nous utilisons un lambda pour le callback. Si vous préférez éviter les lambdas, remplacez-le par une classe anonyme.
        shopAdapter = new ShopAdapter(shopList, new ShopAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String shopName, int storeId) {
                openShopDetail(shopName, storeId);
            }
        });
        shopRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        shopRecyclerView.setAdapter(shopAdapter);
    }

    /**
     * Lance l'activité ShopDetailActivity en passant le nom du magasin et son ID.
     * @param shopName Le nom du magasin
     * @param storeId  L'identifiant du magasin
     */
    private void openShopDetail(String shopName, int storeId) {
        Intent intent = new Intent(this, ShopDetailActivity.class);
        intent.putExtra("shopName", shopName);
        intent.putExtra("storeId", storeId);
        startActivity(intent);
    }

    /**
     * Récupère les magasins depuis le serveur.
     * Utilise Retrofit pour faire un appel asynchrone.
     */
    private void fetchMagasins() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        // Maintenant, on s'attend à recevoir une liste d'objets Shop.
        Call<List<Shop>> call = apiService.getShops();

        call.enqueue(new Callback<List<Shop>>() {
            @Override
            public void onResponse(Call<List<Shop>> call, Response<List<Shop>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Vider la liste locale et ajouter les magasins reçus
                    shopList.clear();
                    for (Shop shop : response.body()) {
                        shopList.add(shop);
                    }
                    shopAdapter.notifyDataSetChanged();
                    Log.d("Retrofit", "Magasins récupérés avec succès!");
                } else {
                    Log.e("Retrofit", "Erreur lors de la récupération des magasins: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Shop>> call, Throwable t) {
                Log.e("Retrofit", "Erreur de connexion: " + t.getMessage());
            }
        });
    }
}
