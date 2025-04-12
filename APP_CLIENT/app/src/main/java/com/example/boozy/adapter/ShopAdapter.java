package com.example.boozy.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.boozy.R;
import com.example.boozy.data.model.Magasin;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<Magasin> magasins;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String shopName, int storeId);
    }

    public ShopAdapter(List<Magasin> magasins, OnItemClickListener listener) {
        this.magasins = magasins;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Magasin magasin = magasins.get(position);
        holder.shopNameText.setText(magasin.getName());

        Glide.with(holder.itemView.getContext())
                .load("http://4.172.255.120:5000/images/" + magasin.getImageNom())
                .placeholder(R.drawable.ic_saq)
                .into(holder.shopImageView);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(magasin.getName(), magasin.getStoreId()));
    }

    @Override
    public int getItemCount() {
        return magasins.size();
    }

    public static class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopNameText;
        ImageView shopImageView;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            shopNameText = itemView.findViewById(R.id.magasinNomText);
            shopImageView = itemView.findViewById(R.id.shopLogo);
        }
    }
}
