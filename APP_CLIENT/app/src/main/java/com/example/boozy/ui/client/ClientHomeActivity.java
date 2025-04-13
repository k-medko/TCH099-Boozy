package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

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
    private List<Magasin> magasinList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_client_home);

        initializeRecyclerView();
        fetchMagasins();
        setupSearchBar();

        findViewById(R.id.btnProfil).setOnClickListener(v -> {
            Intent intent = new Intent(ClientHomeActivity.this, ProfilClientActivity.class);
            startActivity(intent);
        });
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
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<List<Magasin>> call = apiService.getMagasins();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<Magasin>> call, Response<List<Magasin>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    magasinList.clear();
                    magasinList.addAll(response.body());
                    shopAdapter.updateList(new ArrayList<>(magasinList));
                    Log.d("Retrofit", "Nombre de magasins reçus : " + response.body().size());
                } else {
                    Log.e("Retrofit", "Erreur lors de la récupération des magasins : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Magasin>> call, Throwable t) {
                Log.e("Retrofit", "Erreur de connexion : " + t.getMessage());
            }
        });
    }

    private void setupSearchBar() {
        EditText searchBar = findViewById(R.id.searchBar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();
                List<Magasin> filtered = new ArrayList<>();
                for (Magasin m : magasinList) {
                    if (m.getName().toLowerCase().contains(query)) {
                        filtered.add(m);
                    }
                }
                shopAdapter.updateList(filtered);
            }
        });
    }
}
