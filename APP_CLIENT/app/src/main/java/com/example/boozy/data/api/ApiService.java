package com.example.boozy.data.api;

import com.example.boozy.data.model.Adresse;
import com.example.boozy.data.model.UserLoginData;
import com.example.boozy.data.model.Utilisateur;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

public interface ApiService {
    @GET("/getStores?")
    Call<List<List<Object>>> getMagasins();

    @GET("/getProducts")
    Call<List<List<Object>>> getProducts(@Query("storeId") int storeId);

    @GET("getProduct/{id}")
    Call<List<List<Object>>> getProductById(@Path("id") int productId);

    @POST("/connectUser")
    Call<Utilisateur> connectUser(@Body UserLoginData data);

    @GET("/getUser/{id}")
    Call<Utilisateur> getUserById(@Path("id") int userId);

    @GET("/getUserAddress/{userId}")
    Call<Adresse> getUserAddress(@Path("userId") int userId);




}
