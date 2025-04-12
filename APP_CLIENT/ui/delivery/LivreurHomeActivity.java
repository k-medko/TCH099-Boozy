package com.example.boozy.ui.delivery;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.CommandeDisponibleAdapter;
import com.example.boozy.data.model.ClientOrder;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.order.CommandeEnCoursActivity;
import com.example.boozy.ui.order.HistoriqueActivity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LivreurHomeActivity extends AppCompatActivity {

    private LinearLayout btnNavigate;

    private RecyclerView recyclerView;
    private CommandeDisponibleAdapter adapter;
    private final List<ClientOrder> clientOrderList = new ArrayList<>();

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private final ObjectMapper mapper       = new ObjectMapper();

    private static final String GOOGLE_API_KEY     = "AIzaSyD8XPxmyNpeUnJmuBqMZ0un-IXkD8X2Mng";
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";
    private static final String BASE_URL           = "http://4.172.252.189:5000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livreur_home);
        fullScreen();

        /* RecyclerView */
        recyclerView = findViewById(R.id.recyclerCommandes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnNavigate = findViewById(R.id.buttonNavigate);
        btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clientOrderList.isEmpty()) {
                    openNavigationForOrder(clientOrderList.get(0));
                }else {
                    showToast("Aucune Commande Disponible!");
                }
            }
        });

        adapter = new CommandeDisponibleAdapter(
                clientOrderList,
                new CommandeDisponibleAdapter.OnAccepterClickListener() {
                    @Override public void onAccepterClicked(ClientOrder order) {
                        claimOrder(order.getClientOrderId());
                        clientOrderList.remove(order);
                        adapter.notifyDataSetChanged();

                        Intent i = new Intent(LivreurHomeActivity.this, CommandeEnCoursActivity.class);
                        i.putExtra("orderId", order.getClientOrderId());
                        startActivity(i);
                    }
                });

        recyclerView.setAdapter(adapter);

        /* Boutons */
        LinearLayout btnHist = findViewById(R.id.buttonHistorique);
        btnHist.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(LivreurHomeActivity.this, HistoriqueActivity.class));
            }
        });

        LinearLayout btnEnCours = findViewById(R.id.buttonCommandesEnCours);
        btnEnCours.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(LivreurHomeActivity.this, CommandeEnCoursActivity.class));
            }
        });

        LinearLayout btnProfil = findViewById(R.id.buttonProfil);
        btnProfil.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(LivreurHomeActivity.this, ProfilLivreurActivity.class));
            }
        });

        loadAvailableOrders();
    }

    /* ----------------------- API : commandes dispo ----------------------- */
    private void loadAvailableOrders() {

        int carrierId = UtilisateurManager.getInstance(this).getId();
        String url = BASE_URL + "/getAvailableOrders?carrierId=" + carrierId;

        Request rq = new Request.Builder().url(url).build();

        okHttpClient.newCall(rq).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showToast("Réseau indisponible");
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        showToast("Erreur serveur : " + response.code());
                        return;
                    }
                    List<ClientOrder> list = mapper.readValue(
                            response.body().string(),
                            new TypeReference<List<ClientOrder>>() {});

                    clientOrderList.clear();
                    clientOrderList.addAll(list);

                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                    for (ClientOrder co : list) calculateEta(co);

                } finally { response.close(); }
            }
        });
    }

    /**
     * Google ETA
     * @param order la destination
     */
    private void calculateEta(final ClientOrder order) {
        try {
            //on utilise des adresses renvoyes par API pour chaque commande
            String origin      = URLEncoder.encode(order.getShopAddress(), "UTF-8");
            String destination = URLEncoder.encode(order.getClientAddress(), "UTF-8");

            String url = DIRECTIONS_API_URL +
                    "?origin="      + origin +
                    "&destination=" + destination +
                    "&key="         + GOOGLE_API_KEY;

            Request rq = new Request.Builder().url(url).build();

            okHttpClient.newCall(rq).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        if (!response.isSuccessful()) return;

                        JsonNode root = mapper.readTree(response.body().string());
                        JsonNode routes = root.path("routes");

                        if(routes.isMissingNode() || routes.size() == 0 ) return;

                        JsonNode legs = routes.get(0).path("legs");
                        if (legs.isMissingNode() || legs.size() == 0 ) return;

                        String duration = legs.get(0).path("duration").path("text").asText();
                        order.setTempsEstime(duration);

                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                adapter.notifyDataSetChanged();
                            }
                        });
                    } finally { response.close(); }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* -------------------- Réclamer une commande -------------------- */
    private void claimOrder(int orderId) {

        int carrierId = UtilisateurManager.getInstance(this).getId();

        RequestBody body = new FormBody.Builder()
                .add("orderId",   String.valueOf(orderId))
                .add("carrierId", String.valueOf(carrierId))
                .build();

        Request rq = new Request.Builder()
                .url(BASE_URL + "/claimOrder")
                .post(body)
                .build();

        okHttpClient.newCall(rq).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showToast("Impossible de réclamer la commande");
            }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) {
                response.close();
            }
        });
    }

    public void openNavigationForOrder(ClientOrder order) {
        String destination = order.getClientAddress();
        try {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + URLEncoder.encode(destination, "UTF-8"));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(this, "Google Maps non disponible", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur dans l'ouverture de Google Maps", Toast.LENGTH_SHORT).show();
        }
    }

    /* ------------------------- Utilitaires ------------------------- */
    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override public void run() {
                Toast.makeText(LivreurHomeActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fullScreen() {
        Window w = getWindow();
        w.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            w.setDecorFitsSystemWindows(true);
        } else {
            w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }


}
