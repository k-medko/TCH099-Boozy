package com.example.boozy.ui.delivery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
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

    private static final String TAG = "DEBUG_COMMANDE";
    private RecyclerView recyclerView;
    private CommandeDisponibleAdapter adapter;
    private List<Commande> commandeList;
    private ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livreur_home);

        setupFullScreen();

        recyclerView = findViewById(R.id.recyclerCommandes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        commandeList = new ArrayList<>();
        adapter = new CommandeDisponibleAdapter(commandeList, this::takeOrderAndRedirect);
        recyclerView.setAdapter(adapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(ApiService.class);

        loadCommandesFromServer();

        findViewById(R.id.buttonHistorique).setOnClickListener(v ->
                startActivity(new Intent(this, HistoriqueActivity.class)));

        findViewById(R.id.buttonCommandesEnCours).setOnClickListener(v -> {
            String email = UtilisateurManager.getInstance(this).getEmail();
            String password = UtilisateurManager.getInstance(this).getPassword();

            Map<String, String> credentials = new HashMap<>();
            credentials.put("email", email);
            credentials.put("password", password);

            api.getUserOrders(credentials).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Map<String, Object>> orders = (List<Map<String, Object>>) response.body().get("orders");

                        if (orders != null && !orders.isEmpty()) {
                            for (Map<String, Object> order : orders) {
                                String status = (String) order.get("status");
                                if ("Shipping".equalsIgnoreCase(status)) {
                                    int orderId = ((Double) order.get("order_id")).intValue();
                                    String shopName = (String) order.get("shop_name");
                                    String shopAddress = (String) order.get("shop_address");
                                    double totalAmount = (Double) order.get("total_amount");

                                    UtilisateurManager.getInstance(LivreurHomeActivity.this).setDerniereCommande(orderId, shopName, shopAddress, totalAmount);

                                    Intent intent = new Intent(LivreurHomeActivity.this, CommandeEnCoursActivity.class);
                                    intent.putExtra("orderId", orderId);
                                    intent.putExtra("shopName", shopName);
                                    intent.putExtra("shopAddress", shopAddress);
                                    intent.putExtra("totalAmount", totalAmount);
                                    startActivity(intent);
                                    return;
                                }
                            }
                        }

                        Toast.makeText(LivreurHomeActivity.this, "Aucune commande en cours", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LivreurHomeActivity.this, "Erreur lors du chargement", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(LivreurHomeActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            });
        });



        findViewById(R.id.buttonProfil).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilLivreurActivity.class)));
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
        Log.d(TAG, "Chargement des commandes disponibles...");
        api.getOrders().enqueue(new Callback<List<OrderResponse>>() {
            @Override
            public void onResponse(Call<List<OrderResponse>> call, Response<List<OrderResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    commandeList.clear();
                    for (OrderResponse res : response.body()) {
                        String montant = String.format("Total payé : %.2f$", res.getTotalAmount());
                        Commande commande = new Commande(
                                String.valueOf(res.getOrderId()),
                                res.getShopName(),
                                res.getShopAddress(),
                                montant
                        );
                        commandeList.add(commande);
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

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("order_id", orderId);

        api.takeOrder(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().get("status"))) {
                    Map<String, Object> orderMap = (Map<String, Object>) response.body().get("order");
                    int newOrderId = ((Double) orderMap.get("order_id")).intValue();
                    String shopName = (String) orderMap.get("shop_name");
                    String shopAddress = (String) orderMap.get("shop_address");
                    double totalAmount = (Double) orderMap.get("total_amount");

                    UtilisateurManager.getInstance(LivreurHomeActivity.this).setDerniereCommande(newOrderId, shopName, shopAddress, totalAmount);

                    for (int i = 0; i < commandeList.size(); i++) {
                        if (commandeList.get(i).getNumeroCommande().equals(String.valueOf(newOrderId))) {
                            commandeList.remove(i);
                            adapter.notifyItemRemoved(i);
                            break;
                        }
                    }

                    Intent intent = new Intent(LivreurHomeActivity.this, CommandeEnCoursActivity.class);
                    intent.putExtra("orderId", newOrderId);
                    intent.putExtra("shopName", shopName);
                    intent.putExtra("shopAddress", shopAddress);
                    intent.putExtra("totalAmount", totalAmount);
                    startActivity(intent);
                } else {
                    Toast.makeText(LivreurHomeActivity.this, "Échec de l'acceptation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(LivreurHomeActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
