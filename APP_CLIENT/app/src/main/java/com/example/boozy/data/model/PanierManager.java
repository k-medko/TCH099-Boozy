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
        prefs.edit().remove(CART_KEY).apply();
    }

    private void saveCart(List<Produit> panier) {
        String json = new Gson().toJson(panier);
        prefs.edit().putString(CART_KEY, json).apply();
    }
}
