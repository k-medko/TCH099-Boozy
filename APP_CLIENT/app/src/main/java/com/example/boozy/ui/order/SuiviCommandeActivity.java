package com.example.boozy.ui.order;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.api.ApiService;
import com.example.boozy.data.model.UtilisateurManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SuiviCommandeActivity extends AppCompatActivity {

    private TextView numeroCommande, dateCommande, montantCommande, livreurCommande;
    private View ligne1, ligne2, ligne3;

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
        fetchOrderStatus();
    }

    private void setupViews() {
        numeroCommande = findViewById(R.id.numeroCommande);
        dateCommande = findViewById(R.id.dateCommande);
        montantCommande = findViewById(R.id.montantCommande);
        livreurCommande = findViewById(R.id.livreurCommande);
        ligne1 = findViewById(R.id.ligne1);
        ligne2 = findViewById(R.id.ligne2);
        ligne3 = findViewById(R.id.ligne3);
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchOrderStatus() {
        int orderId = getIntent().getIntExtra("order_id", -1);
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
                            String date = (String) order.get("creation_date");
                            String montant = order.get("total_amount") + "$";
                            String status = (String) order.get("status");

                            String livreur = (String) order.get("carrier_name");
                            if (livreur == null || livreur.equalsIgnoreCase("Unassigned Dummy")) {
                                livreurCommande.setText("En recherche d’un livreur...");
                            } else {
                                livreurCommande.setText(livreur);
                            }


                            numeroCommande.setText(String.valueOf(id));
                            dateCommande.setText(date);
                            montantCommande.setText(montant);


                            updateProgression(status);
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

        ligne1.setBackgroundColor(green);

        if ("Shipping".equalsIgnoreCase(status)) {
            ligne2.setBackgroundColor(green);
        } else if ("Completed".equalsIgnoreCase(status)) {
            ligne2.setBackgroundColor(green);
            ligne3.setBackgroundColor(green);
        }
    }
}
