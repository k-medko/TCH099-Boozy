package com.example.boozy.ui.order;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SuiviCommandeActivity extends AppCompatActivity {

    private TextView txtStatut;
    private TextView c1,c2,c3;
    private View  l1,l2,l3;
    private ProgressBar progress;

    private String numeroCommande;

    private OkHttpClient okHttpClient;
    private ObjectMapper mapper;

    //refresh periodique
    private final Handler handler = new Handler();
    private final int REFRESH_MS = 10_000; //refresh toutes les 10s
    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            fetchStatus();
            handler.postDelayed(this, REFRESH_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suivi_commande);

        setFullScreen();

        //recupe du #
        numeroCommande = getIntent().getStringExtra("numeroCommande");

        //init okhttp + jackson
        okHttpClient = new OkHttpClient();
        mapper = new ObjectMapper();

        //ref UI
        txtStatut = findViewById(R.id.textStatut);
        progress = findViewById(R.id.progressSuivi);

        c1 = findViewById(R.id.circle1);
        c2 = findViewById(R.id.circle2);
        c3 = findViewById(R.id.circle3);

        l1 = findViewById(R.id.ligne1);
        l2 = findViewById(R.id.ligne2);
        l3 = findViewById(R.id.ligne3);

        fetchStatus();

    }

    private void setFullScreen(){
        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    /**
     * GET /getOrderStatus?orderId
     */
    private void fetchStatus(){
        if(numeroCommande == null ) return;

        String url = "http://4.172.252.189:5000/getOrderStatus?orderId="
                + "?orderId=" + numeroCommande;

        Request rq = new Request.Builder().url(url).build();

        okHttpClient.newCall(rq).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                showToast("Impossible de recuperer le statut!");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                try{
                    if(response.isSuccessful()){
                        JsonNode root = mapper.readTree(response.body().string());
                        final String status = root.path("status").asText(); // ex. "en_route"

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {updateUI(status);}
                        });
                    }else {
                        showToast("ERREUR SERVEUR :  " + response.code());
                    }
                }finally {
                    response.close();
                }
            }
        });
    }

    /**
     * Aide visuelle pour la suivi commande
     * @param circle etape
     * @param line bridge de letape
     * @param active brun si true et gris si false
     */
    private void setStepActive(TextView circle , View line, boolean active){
        if (active) {
            circle.setBackgroundResource(R.drawable.circle_brown);
            if(line  != null) line.setBackgroundColor(getColor(R.color.brown));
        }else {
            circle.setBackgroundResource(R.drawable.circle_grey);
            if(line != null ) line.setBackgroundColor(getColor(R.color.grey));
        }
    }
    /**
     * Met a jour l'ecran selon le statut renvoye
     * @param status pending, en route ou livree
     */
    private void updateUI( String status){
        switch (status){
            case "pending" :
                txtStatut.setText("Preparation en cours...");
                progress.setProgress(25);

                setStepActive(c1, null, true);
                setStepActive(c2, l1, false);
                setStepActive(c3, l2, false);
                break;

            case "en_route":
                txtStatut.setText("Votre commande est en route");
                progress.setProgress(75);

                setStepActive(c1, null, true);
                setStepActive(c2, l1, true);
                setStepActive(c3, l2, false);
                l3.setBackgroundColor(getColor(R.color.grey));

                break;

            case "delivered":
                txtStatut.setText("Commande livree!");
                progress.setProgress(100);

                setStepActive(c1, null, true);
                setStepActive(c2, l1, true);
                setStepActive(c3, l2, true);
                l3.setBackgroundColor(getColor(R.color.brown));

                handler.removeCallbacks(pollRunnable); // arrete le polling
                break;

            default:
                txtStatut.setText("Statut inconnu");
                progress.setProgress(0);

                setStepActive(c1, null, false);
                setStepActive(c2, l1, false);
                setStepActive(c3, l2, false);
                l3.setBackgroundColor(getColor(R.color.grey));

        }
    }

    private void showToast (final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SuiviCommandeActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    //lancement / arret du polling
    @Override protected void onResume() {
        super.onResume();
        handler.postDelayed(pollRunnable, REFRESH_MS);
    }
    @Override protected void onPause(){
        super.onPause();
        handler.removeCallbacks(pollRunnable);
    }

}
