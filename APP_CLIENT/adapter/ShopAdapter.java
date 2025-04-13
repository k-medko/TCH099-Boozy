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
import com.example.boozy.data.model.Shop;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<Shop> shops;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String shopName, int storeId);
    }

    public ShopAdapter(List<Shop> shops, OnItemClickListener listener) {
        this.shops = shops;
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
        Shop shop = shops.get(position);
        holder.shopNameText.setText(shop.getName());

        Glide.with(holder.itemView.getContext())
                .load("http://4.172.255.120:5000/images/" + shop.getImageNom())
                .placeholder(R.drawable.ic_saq)
                .into(holder.shopImageView);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(shop.getName(), shop.getShopId()));
    }

    @Override
    public int getItemCount() {
        return shops.size();
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
