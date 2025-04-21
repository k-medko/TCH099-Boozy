package com.example.boozy.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.boozy.R;
import com.example.boozy.data.model.Produit;
import com.example.boozy.ui.shop.ProductDetailActivity;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Produit> produits;

    public ProductAdapter(List<Produit> produits) {
        this.produits = produits;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Produit produit = produits.get(position);

        holder.productNameText.setText(produit.getName());
        holder.productCategoryText.setText(produit.getCategory());
        holder.productPriceText.setText(String.format("$%.2f", produit.getPrice()));

        String imageUrl = "http://4.172.252.189:5000/images/" + produit.getImageName();
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.produit)
                .error(R.drawable.produit)
                .into(holder.productImage);

        if (produit.getStock() <= 0) {
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setClickable(false);
            holder.itemView.setFocusable(false);
        } else {
            holder.itemView.setAlpha(1f);
            holder.itemView.setClickable(true);
            holder.itemView.setFocusable(true);

            holder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product_id", produit.getId());
                intent.putExtra("product_name", produit.getName());
                intent.putExtra("product_price", produit.getPrice());
                intent.putExtra("product_image_name", produit.getImageName());
                intent.putExtra("product_description", produit.getDescription());
                intent.putExtra("shop_id", produit.getShopId());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return produits.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productNameText, productCategoryText, productPriceText;
        ImageView productImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productNameText = itemView.findViewById(R.id.productNameText);
            productCategoryText = itemView.findViewById(R.id.productCategoryText);
            productPriceText = itemView.findViewById(R.id.productPriceText);
            productImage = itemView.findViewById(R.id.productImage);
        }
    }

    public void updateProductList(List<Produit> newList) {
        this.produits = newList;
        notifyDataSetChanged();
    }
}
