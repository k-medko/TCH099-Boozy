package com.example.boozy.ui.order;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.UtilisateurManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SuiviCommandeActivity extends AppCompatActivity {

    private TextView numeroCommande, dateCommande, montantCommande, livreurCommande;
    private View ligne1, ligne2, ligne3;
    private Button buttonAnnuler;

    private final Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL_MS = 1000;
    private int orderId;
    private String currentStatus = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suivi_commande);

        setupViews();
        setupBackButton();

        orderId = getIntent().getIntExtra("order_id", -1);
        fetchOrderStatus();

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                fetchOrderStatus();
                refreshHandler.postDelayed(this, REFRESH_INTERVAL_MS);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    private void setupViews() {
        numeroCommande = findViewById(R.id.numeroCommande);
        dateCommande = findViewById(R.id.dateCommande);
        montantCommande = findViewById(R.id.montantCommande);
        livreurCommande = findViewById(R.id.livreurCommande);
        ligne1 = findViewById(R.id.ligne1);
        ligne2 = findViewById(R.id.ligne2);
        ligne3 = findViewById(R.id.ligne3);
        buttonAnnuler = findViewById(R.id.buttonAnnulerCommande);

        buttonAnnuler.setOnClickListener(v -> cancelOrder());
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchOrderStatus() {
        if (orderId == -1) return;

        String email = UtilisateurManager.getInstance(this).getEmail();
        String password = UtilisateurManager.getInstance(this).getPassword();

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        api.getUserOrders(body).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Map<String, Object>> orders = (List<Map<String, Object>>) response.body().get("orders");

                    for (Map<String, Object> order : orders) {
                        int id = ((Double) order.get("order_id")).intValue();
                        if (id == orderId) {
                            String rawDate = (String) order.get("creation_date");
                            String montantRaw = order.get("total_amount").toString();
                            String status = (String) order.get("status");
                            String livreur = (String) order.get("carrier_name");

                            currentStatus = status;

                            try {
                                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA_FRENCH);
                                Date date = inputFormat.parse(rawDate);
                                SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy 'à' HH:mm:ss", Locale.CANADA_FRENCH);
                                rawDate = outputFormat.format(date);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            DecimalFormat df = new DecimalFormat("0.00");
                            String montant = df.format(Double.parseDouble(montantRaw)) + "$";

                            numeroCommande.setText(String.valueOf(id));
                            dateCommande.setText(rawDate);
                            montantCommande.setText(montant);

                            if ("Cancelled".equalsIgnoreCase(status)) {
                                livreurCommande.setText("Commande annulée");
                            } else if (livreur == null || livreur.equalsIgnoreCase("Unassigned Dummy")) {
                                livreurCommande.setText("En recherche d’un livreur...");
                            } else {
                                livreurCommande.setText(livreur);
                            }

                            updateProgression(status);
                            updateCancelButtonVisibility(status);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void updateProgression(String status) {
        int green = getColor(R.color.green);

        if ("Cancelled".equalsIgnoreCase(status)) {
            ligne1.setBackgroundColor(Color.GRAY);
            ligne2.setBackgroundColor(Color.GRAY);
            ligne3.setBackgroundColor(Color.GRAY);
        } else {
            ligne1.setBackgroundColor(green);

            if ("Shipping".equalsIgnoreCase(status)) {
                ligne2.setBackgroundColor(green);
            } else if ("Completed".equalsIgnoreCase(status)) {
                ligne2.setBackgroundColor(green);
                ligne3.setBackgroundColor(green);
            }
        }
    }

    private void updateCancelButtonVisibility(String status) {
        if ("Cancelled".equalsIgnoreCase(status) ||
                "Shipping".equalsIgnoreCase(status) ||
                "Completed".equalsIgnoreCase(status)) {
            buttonAnnuler.setVisibility(View.GONE);
        } else {
            buttonAnnuler.setVisibility(View.VISIBLE);
        }
    }

    private void cancelOrder() {
        if (orderId == -1) return;

        String email = UtilisateurManager.getInstance(this).getEmail();
        String password = UtilisateurManager.getInstance(this).getPassword();

        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("order_id", orderId);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://4.172.252.189:5000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService api = retrofit.create(ApiService.class);

        api.cancelOrder(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    livreurCommande.setText("Commande annulée");
                    updateCancelButtonVisibility("Cancelled");
                    fetchOrderStatus(); // actualiser pour montrer la progression grisée
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
