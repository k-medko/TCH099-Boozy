package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.boozy.R;
import com.example.boozy.data.model.Magasin;
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

public class ClientHomeActivity extends AppCompatActivity {

    private RecyclerView shopRecyclerView;
    private ShopAdapter shopAdapter;
    private List<Magasin> magasinList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_client_home);

        initializeRecyclerView();
        fetchMagasins();
    }

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

    private void initializeRecyclerView() {
        shopRecyclerView = findViewById(R.id.shopRecyclerView);
        magasinList = new ArrayList<>();
        shopAdapter = new ShopAdapter(magasinList, (shopName, storeId) -> openShopDetail(shopName, storeId));
        shopRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        shopRecyclerView.setAdapter(shopAdapter);
    }

    private void openShopDetail(String shopName, int storeId) {
        Intent intent = new Intent(this, ShopDetailActivity.class);
        intent.putExtra("shopName", shopName);
        intent.putExtra("storeId", storeId);
        startActivity(intent);
    }

    private void fetchMagasins() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.255.120:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<List<Object>>> call = apiService.getMagasins();

        call.enqueue(new Callback<List<List<Object>>>() {
            @Override
            public void onResponse(Call<List<List<Object>>> call, Response<List<List<Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    magasinList.clear();
                    for (List<Object> data : response.body()) {
                        int storeId = ((Double) data.get(0)).intValue();
                        String name = (String) data.get(1);
                        int addressId = ((Double) data.get(2)).intValue();
                        String imageNom = (String) data.get(3);

                        Magasin magasin = new Magasin(storeId, name, addressId, imageNom);
                        magasinList.add(magasin);
                    }
                    shopAdapter.notifyDataSetChanged();
                    Log.d("Retrofit", "Magasins récupérés avec succès!");
                } else {
                    Log.e("Retrofit", "Erreur lors de la récupération des magasins : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<List<Object>>> call, Throwable t) {
                Log.e("Retrofit", "Erreur de connexion : " + t.getMessage());
            }
        });
    }

}
