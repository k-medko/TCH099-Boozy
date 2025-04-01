package com.example.boozy.ui.delivery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.adapter.CommandeDisponibleAdapter;
import com.example.boozy.data.model.Commande;
import com.example.boozy.ui.order.CommandeEnCoursActivity;
import com.example.boozy.ui.order.HistoriqueActivity;
import com.example.boozy.ui.delivery.ProfilLivreurActivity;

import java.util.ArrayList;
import java.util.List;

public class LivreurHomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommandeDisponibleAdapter adapter;
    private List<Commande> commandeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livreur_home);

        // Effet plein écran
        setupFullScreen();

        // Initialisation des vues
        recyclerView = findViewById(R.id.recyclerCommandes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialiser la liste des commandes
        commandeList = new ArrayList<>();

        // Charger les commandes depuis le serveur
        loadCommandesFromServer();

        // Configuration de l'adaptateur
        adapter = new CommandeDisponibleAdapter(commandeList, commande -> {
            commandeList.remove(commande);
            adapter.notifyDataSetChanged();

            Intent intent = new Intent(LivreurHomeActivity.this, CommandeEnCoursActivity.class);
            intent.putExtra("numeroCommande", commande.getNumeroCommande());
            intent.putExtra("magasin", commande.getMagasin());
            intent.putExtra("adresse", commande.getAdresseLivraison());
            intent.putExtra("montant", commande.getMontant());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Bouton de redirection vers l'historique
        LinearLayout btnHistorique = findViewById(R.id.buttonHistorique);
        btnHistorique.setOnClickListener(v -> {
            Intent intent = new Intent(LivreurHomeActivity.this, HistoriqueActivity.class);
            startActivity(intent);
        });

        // Bouton de redirection vers la commande en cours
        LinearLayout btnCommandeEnCours = findViewById(R.id.buttonCommandesEnCours);
        btnCommandeEnCours.setOnClickListener(v -> {
            Intent intent = new Intent(LivreurHomeActivity.this, CommandeEnCoursActivity.class);
            startActivity(intent);
        });

        // Bouton de redirection vers le profil livreur
        LinearLayout btnProfilLivreur = findViewById(R.id.buttonProfil);
        btnProfilLivreur.setOnClickListener(v -> {
            Intent intent = new Intent(LivreurHomeActivity.this, com.example.boozy.ui.delivery.ProfilLivreurActivity.class);
            startActivity(intent);
        });
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

    /**
     * Charger les commandes depuis le serveur
     */
    private void loadCommandesFromServer() {
        // APPEL À L'API ICI
        // Effectuer une requête GET pour récupérer la liste des commandes en cours de traitement.

        adapter.notifyDataSetChanged();
    }

    /**
     * Calculer le temps estimé
     */
    private void calculateEstimatedTime(Commande commande) {
        // APPEL À L'API ICI
        // Effectuer une requête à l'API Google Maps estimer le temps de livraison.

        adapter.notifyDataSetChanged();
    }
}
