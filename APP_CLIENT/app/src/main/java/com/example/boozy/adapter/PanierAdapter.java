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

    public interface OnQuantityChangeListener {
        void onQuantityChanged(List<Produit> updatedList);
    }

    public interface OnPanierChangedListener {
        void onPanierUpdated(List<Produit> updatedList);
    }

    public PanierAdapter(Context context, List<Produit> produits, OnQuantityChangeListener listener) {
        this.context = context;
        this.productList = produits;
        this.listener = listener;
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

        double price = produit.getPrice();

        holder.prixText.setText(String.format("%.2f $", price));

        holder.buttonPlus.setOnClickListener(v -> {
            produit.setQuantity(produit.getQuantity() + 1);
            notifyItemChanged(position);
            listener.onQuantityChanged(productList);
            updateCartPersistence();
        });

        holder.buttonMinus.setOnClickListener(v -> {
            if (produit.getQuantity() > 1) {
                produit.setQuantity(produit.getQuantity() - 1);
                notifyItemChanged(position);
                listener.onQuantityChanged(productList);
                updateCartPersistence();
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {

            productList.remove(position);
            updateCartPersistence();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, productList.size());

            if (panierChangedListener != null) {
                panierChangedListener.onPanierUpdated(new ArrayList<>(productList));
            }

            listener.onQuantityChanged(productList);

            Toast.makeText(context, "Produit supprimé", Toast.LENGTH_SHORT).show();
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProductList(List<Produit> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    private void updateCartPersistence() {
        PanierManager panierManager = PanierManager.getInstance(context);
        panierManager.clearCart();

        if (!productList.isEmpty()) {
            String shopId = productList.get(0).getShopId();
            panierManager.setCurrentShopId(shopId);

            for (Produit produit : productList) {
                panierManager.addProduct(produit);
            }
        }
    }



}
