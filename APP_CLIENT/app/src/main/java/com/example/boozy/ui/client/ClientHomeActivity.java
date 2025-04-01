package com.example.boozy.ui.client;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.boozy.R;
import com.example.boozy.data.model.Magasin;
import com.example.boozy.adapter.ShopAdapter;
import com.example.boozy.ui.shop.ShopDetailActivity;

import java.util.ArrayList;
import java.util.List;

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
        shopAdapter = new ShopAdapter(magasinList, magasin -> openShopDetail(magasin.getName()));
        shopRecyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        shopRecyclerView.setAdapter(shopAdapter);
    }

    private void openShopDetail(String shopName) {
        Intent intent = new Intent(this, ShopDetailActivity.class);
        intent.putExtra("shopName", shopName);
        startActivity(intent);
    }

    private void fetchMagasins() {
        // APPEL À L'API ICI
        // Faire un appel à l'API pour récupérer les magasins.
    }
}
