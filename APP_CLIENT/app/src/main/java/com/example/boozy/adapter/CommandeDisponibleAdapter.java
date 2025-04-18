package com.example.boozy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.data.model.Commande;

import java.util.List;

public class CommandeDisponibleAdapter extends RecyclerView.Adapter<CommandeDisponibleAdapter.ViewHolder> {

    public interface OnAccepterClickListener {
        void onAccepterClicked(Commande commande);
    }

    private List<Commande> commandes;
    private OnAccepterClickListener listener;

    public CommandeDisponibleAdapter(List<Commande> commandes, OnAccepterClickListener listener) {
        this.commandes = commandes;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numeroCommande, nomMagasin, adresseText, totalText;
        Button accepterButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            numeroCommande = itemView.findViewById(R.id.numeroCommande);
            nomMagasin = itemView.findViewById(R.id.nomMagasin);
            adresseText = itemView.findViewById(R.id.adresseText);
            totalText = itemView.findViewById(R.id.totalText);
            accepterButton = itemView.findViewById(R.id.accepterButton);
        }
    }

    @NonNull
    @Override
    public CommandeDisponibleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commande_livreur, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandeDisponibleAdapter.ViewHolder holder, int position) {
        Commande commande = commandes.get(position);

        holder.numeroCommande.setText("Commande #" + commande.getNumeroCommande());
        holder.nomMagasin.setText(commande.getMagasin());
        holder.adresseText.setText(commande.getAdresseLivraison());
        holder.totalText.setText(commande.getMontant());

        holder.accepterButton.setOnClickListener(v -> listener.onAccepterClicked(commande));
    }

    @Override
    public int getItemCount() {
        return commandes.size();
    }
}
