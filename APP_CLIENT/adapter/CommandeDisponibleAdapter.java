package com.example.boozy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.data.model.ClientOrder;

import java.util.List;

public class CommandeDisponibleAdapter extends RecyclerView.Adapter<CommandeDisponibleAdapter.ViewHolder> {

    public interface OnAccepterClickListener {
        void onAccepterClicked(ClientOrder clientOrder);
    }

    private List<ClientOrder> clientOrders;
    private OnAccepterClickListener listener;

    public CommandeDisponibleAdapter(List<ClientOrder> clientOrders, OnAccepterClickListener listener) {
        this.clientOrders = clientOrders;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numeroCommande, tempsEstimeText, paiementText;
        Button accepterButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            numeroCommande = itemView.findViewById(R.id.numeroCommande);
            tempsEstimeText = itemView.findViewById(R.id.tempsEstimeText);
            paiementText = itemView.findViewById(R.id.paiementText);
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
        ClientOrder clientOrder = clientOrders.get(position);

        // Numéro de commande
        holder.numeroCommande.setText("Commande " + clientOrder.getClientOrderId());

        // Temps estimé avec fallback
        String temps = clientOrder.getTempsEstime();
        if (temps != null && !temps.isEmpty()) {
            holder.tempsEstimeText.setText("Temps approximatif : " + temps);
        } else {
            holder.tempsEstimeText.setText("Temps approximatif : Calcul en cours...");
        }

        // Montant a calculer
        holder.paiementText.setText("Montant : " + clientOrder.getTotalAmount());

        // Action bouton
        holder.accepterButton.setOnClickListener(v -> listener.onAccepterClicked(clientOrder));

    }

    @Override
    public int getItemCount() {
        return clientOrders.size();
    }
}
