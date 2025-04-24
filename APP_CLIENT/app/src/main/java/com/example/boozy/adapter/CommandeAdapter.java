package com.example.boozy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.data.model.Commande;

import java.util.List;

public class CommandeAdapter extends RecyclerView.Adapter<CommandeAdapter.CommandeViewHolder> {

    private List<Commande> commandes;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Commande commande);
    }

    public CommandeAdapter(List<Commande> commandes, OnItemClickListener listener) {
        this.commandes = commandes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commande, parent, false);
        return new CommandeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommandeViewHolder holder, int position) {
        Commande commande = commandes.get(position);
        holder.numero.setText("Commande #" + commande.getOrderId());
        holder.date.setText("Date : " + commande.getCreationDate());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(commande));
    }

    @Override
    public int getItemCount() {
        return commandes.size();
    }

    static class CommandeViewHolder extends RecyclerView.ViewHolder {
        TextView numero, date;

        public CommandeViewHolder(@NonNull View itemView) {
            super(itemView);
            numero = itemView.findViewById(R.id.textNumero);
            date = itemView.findViewById(R.id.textDate);
        }
    }
}
