package com.example.boozy.ui.order;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.CommandeAdapter;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.Commande;
import com.example.boozy.data.model.TypeUtilisateur;
import com.example.boozy.data.model.UtilisateurManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HistoriqueActivity extends AppCompatActivity {

    private RecyclerView recyclerHistorique;
    private CommandeAdapter commandeAdapter;
    private List<Commande> commandes = new ArrayList<>();
    private boolean isClient = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        setupFullScreen();
        setupBackButton();

        recyclerHistorique = findViewById(R.id.recyclerHistorique);

        TypeUtilisateur type = UtilisateurManager.getInstance(this).getType();
        isClient = type == TypeUtilisateur.CLIENT;

        commandeAdapter = new CommandeAdapter(commandes, commande -> {
            if (isClient) {
                Intent intent = new Intent(HistoriqueActivity.this, SuiviCommandeActivity.class);
                intent.putExtra("order_id", commande.getOrderId());
                startActivity(intent);
            }
        });

        recyclerHistorique.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistorique.setAdapter(commandeAdapter);

        String email = UtilisateurManager.getInstance(this).getEmail();
        String password = UtilisateurManager.getInstance(this).getPassword();

        fetchUserOrders(email, password);
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

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchUserOrders(String email, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Call<Map<String, Object>> call = api.getUserOrders(body);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String role = String.valueOf(response.body().get("user_role"));
                    Log.d("ROLE", "Connecté en tant que : " + role);

                    Gson gson = new Gson();
                    String ordersJson = gson.toJson(response.body().get("orders"));
                    Type listType = new TypeToken<List<Commande>>() {}.getType();
                    List<Commande> fetched = gson.fromJson(ordersJson, listType);

                    commandes.clear();
                    commandes.addAll(fetched);
                    commandeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
