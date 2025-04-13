package com.example.boozy.data.api;

import com.example.boozy.data.model.Adresse;
import com.example.boozy.data.model.LoginResponse;
import com.example.boozy.data.model.Magasin;
import com.example.boozy.data.model.Produit;
import com.example.boozy.data.model.UserLoginData;
import com.example.boozy.data.model.Utilisateur;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import java.util.List;

public interface ApiService {
    @GET("/getShops")
    Call<List<Magasin>> getMagasins();

    @GET("/getProducts")
    Call<List<Produit>> getProducts(@Query("storeId") int storeId);

    @POST("/connectUser")
    Call<LoginResponse> connectUser(@Body UserLoginData data);

    @POST("/createUser")
    Call<Void> createUser(@Body Utilisateur utilisateur);






}
