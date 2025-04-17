package com.example.boozy.ui.delivery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.CommandeDisponibleAdapter;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.Commande;
import com.example.boozy.data.model.OrderResponse;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.order.CommandeEnCoursActivity;
import com.example.boozy.ui.order.HistoriqueActivity;
import com.example.boozy.ui.delivery.ProfilLivreurActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LivreurHomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommandeDisponibleAdapter adapter;
    private List<Commande> commandeList;
    private Retrofit retrofit;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livreur_home);

        setupFullScreen();

        recyclerView = findViewById(R.id.recyclerCommandes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commandeList = new ArrayList<>();

        adapter = new CommandeDisponibleAdapter(commandeList, commande -> {
            takeOrderAndRedirect(commande);
        });

        recyclerView.setAdapter(adapter);

        retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ApiService.class);

        loadCommandesFromServer();

        findViewById(R.id.buttonHistorique).setOnClickListener(v -> startActivity(new Intent(this, HistoriqueActivity.class)));
        findViewById(R.id.buttonCommandesEnCours).setOnClickListener(v -> startActivity(new Intent(this, CommandeEnCoursActivity.class)));
        findViewById(R.id.buttonProfil).setOnClickListener(v -> startActivity(new Intent(this, ProfilLivreurActivity.class)));
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

    private void loadCommandesFromServer() {
        Call<List<OrderResponse>> call = api.getOrders();

        call.enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commandeList.clear();
                    for (OrderResponse res : response.body()) {
                        String montant = "0$ (a faire)";
                        Commande c = new Commande(
                                String.valueOf(res.getOrderId()),
                                res.getShopName(),
                                res.getShopAddress(),
                                montant,
                                "Calcul en cours..."
                        );
                        commandeList.add(c);
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(LivreurHomeActivity.this, "Erreur chargement commandes", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<OrderResponse>> call, Throwable t) {
                Toast.makeText(LivreurHomeActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void takeOrderAndRedirect(Commande commande) {
        String email = UtilisateurManager.getInstance(this).getEmail();
        String password = UtilisateurManager.getInstance(this).getPassword();
        int orderId = Integer.parseInt(commande.getNumeroCommande());

        Log.d("TAKE_ORDER", "Tentative d’acceptation de commande");
        Log.d("TAKE_ORDER", "Email: " + email);
        Log.d("TAKE_ORDER", "Password: " + password);
        Log.d("TAKE_ORDER", "Order ID: " + orderId);

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("order_id", orderId);

        api.takeOrder(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("TAKE_ORDER", "Réponse succès : " + response.body());
                    Toast.makeText(LivreurHomeActivity.this, "Commande acceptée", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LivreurHomeActivity.this, CommandeEnCoursActivity.class);
                    intent.putExtra("numeroCommande", commande.getNumeroCommande());
                    intent.putExtra("magasin", commande.getMagasin());
                    intent.putExtra("adresse", commande.getAdresseLivraison());
                    intent.putExtra("montant", commande.getMontant());
                    startActivity(intent);
                } else {
                    Log.e("TAKE_ORDER", "Échec response code: " + response.code());
                    try {
                        Log.e("TAKE_ORDER", "Erreur: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(LivreurHomeActivity.this, "Échec de l'acceptation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("TAKE_ORDER", "Erreur réseau: " + t.getMessage());
                Toast.makeText(LivreurHomeActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
