package com.example.boozy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.boozy.R;
import com.example.boozy.data.model.PanierManager;
import com.example.boozy.data.model.Produit;

import java.util.ArrayList;
import java.util.List;

public class PanierAdapter extends RecyclerView.Adapter<PanierAdapter.ViewHolder> {

    private Context context;
    private List<Produit> productList;
    private OnQuantityChangeListener listener;
    private OnPanierChangedListener panierChangedListener;

    // Interface pour la quantité
    public interface OnQuantityChangeListener {
        void onQuantityChanged(List<Produit> updatedList);
    }

    // Interface pour la suppression
    public interface OnPanierChangedListener {
        void onPanierUpdated(List<Produit> updatedList);
    }

    //  Constructeur
    public PanierAdapter(Context context, List<Produit> produits, OnQuantityChangeListener listener) {
        this.context = context;
        this.productList = produits;
        this.listener = listener;
    }

    // Setter du listener de suppression (?)
    public void setOnPanierChangedListener(OnPanierChangedListener listener) {
        this.panierChangedListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView quantiteText, produitText, prixText;
        ImageButton buttonPlus, buttonMinus, buttonDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            quantiteText = itemView.findViewById(R.id.quantiteText);
            produitText = itemView.findViewById(R.id.produitText);
            prixText = itemView.findViewById(R.id.prixText);
            buttonPlus = itemView.findViewById(R.id.buttonPlus);
            buttonMinus = itemView.findViewById(R.id.buttonMinus);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

    @NonNull
    @Override
    public PanierAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_paiement, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PanierAdapter.ViewHolder holder, int position) {
        Produit produit = productList.get(position);

        holder.quantiteText.setText(String.valueOf(produit.getQuantity()));
        holder.produitText.setText(produit.getName());
        holder.prixText.setText(String.format("%.2f $", produit.getPrice() / 100.0));

        // Ajouter quantité
        holder.buttonPlus.setOnClickListener(v -> {
            produit.setQuantity(produit.getQuantity() + 1);
            notifyItemChanged(position);
            listener.onQuantityChanged(productList);
            updateCartPersistence();
        });

        // Diminuer quantité
        holder.buttonMinus.setOnClickListener(v -> {
            if (produit.getQuantity() > 1) {
                produit.setQuantity(produit.getQuantity() - 1);
                notifyItemChanged(position);
                listener.onQuantityChanged(productList);
                updateCartPersistence();
            }
        });

        // Supprimer produit
        holder.buttonDelete.setOnClickListener(v -> {
            productList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, productList.size());
            updateCartPersistence();

            if (panierChangedListener != null) {
                panierChangedListener.onPanierUpdated(new ArrayList<>(productList));
            }

            Toast.makeText(context, "Produit supprimé", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Mise à jour externe de la liste
    public void updateProductList(List<Produit> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    // Mise à jour du panier persistant
    private void updateCartPersistence() {
        PanierManager.getInstance(context).clearCart();
        for (Produit p : productList) {
            PanierManager.getInstance(context).addProduct(p);
        }
    }
}
