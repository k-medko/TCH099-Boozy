package com.example.boozy.data.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PanierManager {

    private static final String PREF_NAME = "boozy_prefs";
    private static final String CART_KEY = "cart";
    private static final String CURRENT_SHOP_ID_KEY = "current_shop_id";

    private static PanierManager instance;
    private SharedPreferences prefs;

    private PanierManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PanierManager getInstance(Context context) {
        if (instance == null) {
            instance = new PanierManager(context.getApplicationContext());
        }
        return instance;
    }

    public void addProduct(Produit product) {
        String currentShopId = getCurrentShopId();

        if (currentShopId == null) {
            setCurrentShopId(product.getShopId());
        }

        else if (!currentShopId.equals(product.getShopId())) {
            clearCart();
            setCurrentShopId(product.getShopId());
        }

        List<Produit> panier = getCart();
        panier.add(product);
        saveCart(panier);
    }



    public void removeProduct(Produit product) {
        List<Produit> panier = getCart();
        panier.remove(product);
        saveCart(panier);
    }

    public List<Produit> getCart() {
        String json = prefs.getString(CART_KEY, null);
        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<Produit>>() {}.getType();
        return new Gson().fromJson(json, type);
    }

    public void clearCart() {
        prefs.edit()
                .remove(CART_KEY)
                .remove(CURRENT_SHOP_ID_KEY)
                .apply();
    }

    private void saveCart(List<Produit> panier) {
        String json = new Gson().toJson(panier);
        prefs.edit().putString(CART_KEY, json).apply();
    }

    public void setCurrentShopId(String shopId) {
        prefs.edit().putString(CURRENT_SHOP_ID_KEY, shopId).apply();
    }

    public String getCurrentShopId() {
        return prefs.getString(CURRENT_SHOP_ID_KEY, null);
    }

    public void clearCurrentShopId() {
        prefs.edit().remove(CURRENT_SHOP_ID_KEY).apply();
    }

    public boolean canAddProductFromShop(String shopId) {
        String currentShopId = getCurrentShopId();
        return currentShopId == null || currentShopId.equals(shopId);
    }

}
