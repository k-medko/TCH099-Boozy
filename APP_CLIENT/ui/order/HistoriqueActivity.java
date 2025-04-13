package com.example.boozy.ui.order;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.CommandeAdapter;
import com.example.boozy.data.model.ClientOrder;
import com.example.boozy.data.model.TypeUtilisateur;
import com.example.boozy.data.model.UtilisateurManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoriqueActivity extends AppCompatActivity {

    //UI
    private RecyclerView recyclerView;
    private CommandeAdapter adapter;

    //Donnees
    private final List<ClientOrder> clientOrders = new ArrayList<>();

    //okhttp + jackson
    private OkHttpClient okHttpClient;
    private ObjectMapper mapper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique);

        String userType = UtilisateurManager.getInstance(this).getUserType();

       setFullScreen();

       //init ok + jack
        okHttpClient = new OkHttpClient();
        mapper = new ObjectMapper();

        recyclerView = findViewById(R.id.recyclerHistorique);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CommandeAdapter(clientOrders);
        recyclerView.setAdapter(adapter);

        //telechargement historique
        fetchHistorique();

    }

    private void setFullScreen(){
        Window w = getWindow();
        w.setNavigationBarColor(Color.TRANSPARENT);
        if  (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            w.setDecorFitsSystemWindows(true);
        }else {
            w.getDecorView().setSystemUiVisibility(RecyclerView.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    /**
     * appel GET /getOrderHistory?userId=
     */
    private void fetchHistorique(){

        int userId = UtilisateurManager.getInstance(getApplicationContext()).getId();
        String userType = UtilisateurManager.getInstance(getApplicationContext()).getUserType();

        String url = "http://4.172.252.189:5000/getOrderHistory"
                + "?userId=" + userId;


        Request rq = new Request.Builder().url(url).build();

        okHttpClient.newCall(rq).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                showToast("ERREUR RESEAU : Historique indisponible");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                try{
                    if(response.isSuccessful()){

                        //le serveur renvoie une liste dde maps JSON
                        List<Map<String, Object>> list = mapper.readValue(
                                response.body().string(),
                                new TypeReference<List<Map<String, Object>>>() {});

                        clientOrders.clear();

                        for(Map<String, Object> m : list) {

                            String num = (String) m.get("numeroCommande");
                            String date = (String) m.get("dateCommande");
                            ClientOrder c = new ClientOrder();
                            clientOrders.add(c);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {adapter.notifyDataSetChanged();}
                        });
                    }else{
                        showToast("ERREUR SERVEUR : " + response.code());
                    }
                }finally {
                    response.close();
                }
            }
        });
    }

    /**
     * Toast pour msg
     * @param msg la message quon vt
     */
    private void showToast(String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HistoriqueActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
