package com.example.boozy.data.api;

import com.example.boozy.data.model.AvailabilityResponse;
import com.example.boozy.data.model.LoginResponse;
import com.example.boozy.data.model.Magasin;
import com.example.boozy.data.model.OrderResponse;
import com.example.boozy.data.model.Produit;
import com.example.boozy.data.model.UserLoginData;
import com.example.boozy.data.model.Utilisateur;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("/getShops")
    Call<List<Magasin>> getMagasins();

    @GET("/getProducts")
    Call<List<Produit>> getProductsById(@Query("product_id") int productId);

    @GET("/getAvailability")
    Call<List<AvailabilityResponse>> getAvailabilityByShop(@Query("shop_id") int shopId);

    @POST("/connectUser")
    Call<LoginResponse> connectUser(@Body UserLoginData data);

    @POST("/connectUser")
    Call<Map<String, Object>> connectUser(@Body Map<String, String> body);

    @POST("/createUser")
    Call<Void> createUser(@Body Utilisateur utilisateur);

    @POST("/modifyUser")
    Call<Map<String, Object>> modifyUser(@Body Map<String, Object> body);

    @POST("/createOrder")
    Call<Map<String, Object>> createOrder(@Body Map<String, Object> body);

    @GET("/getOrders")
    Call<List<OrderResponse>> getOrders();

    @POST("/getUserOrders")
    Call<Map<String, Object>> getUserOrders(@Body Map<String, String> body);

    @POST("/takeOrder")
    Call<Map<String, Object>> takeOrder(@Body Map<String, Object> body);

    @POST("/updateOrder")
    Call<Map<String, Object>> updateOrder(@Body Map<String, Object> body);

    @POST("/cancelOrder")
    Call<Map<String, Object>> cancelOrder(@Body Map<String, Object> body);


}
