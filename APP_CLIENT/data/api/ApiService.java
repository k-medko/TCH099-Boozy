package com.example.boozy.data.api;

import com.example.boozy.data.model.ClientOrder;
import com.example.boozy.data.model.CreateOrderResponse;
import com.example.boozy.data.model.LoginDto;
import com.example.boozy.data.model.OrderStatusResponse;
import com.example.boozy.data.model.RegisterDto;
import com.example.boozy.data.model.Shop;
import com.example.boozy.data.model.ShopProduct;
import com.example.boozy.data.model.UserAccount;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * ApiService
 * ----------
 * Interface Retrofit pour communiquer avec votre serveur Flask.
 * Les routes correspondent aux endpoints définis dans le code du serveur.
 */
public interface ApiService {

    /* ----------------------------------------------------------------------
       Routes publiques : Récupération des magasins, des produits, etc.
       ---------------------------------------------------------------------- */

    /**
     * Récupère la liste des magasins via /getShops
     * (Possibilité de passer un shop_id en paramètre GET,
     *  mais ici on récupère généralement tous les magasins).
     */
    @GET("/getShops")
    Call<List<Shop>> getShops();

    /**
     * Récupère la disponibilité (inventaire) de produits dans un magasin
     * via l’endpoint /getAvailability.
     * Paramètres optionnels : shop_id, product_id, in_stock
     * Si vous souhaitez récupérer tous les produits d’un magasin particulier,
     * vous pouvez appeler getAvailability(shopId, null, null).
     */
    @GET("/getAvailability")
    Call<List<ShopProduct>> getAvailability(
            @Query("shop_id") Integer shopId,
            @Query("product_id") Integer productId,
            @Query("in_stock") Integer inStock
    );

    /**
     * Récupère la liste de tous les produits ou d’une catégorie spécifique
     * via /getProducts.
     * Paramètres optionnels : product_id, category
     */
    @GET("/getProducts")
    Call<List<ShopProduct>> getProducts(
            @Query("product_id") Integer productId,
            @Query("category") String category
    );

    /* ----------------------------------------------------------------------
       Authentification et gestion des utilisateurs
       ---------------------------------------------------------------------- */

    /**
     * Connecte un utilisateur via /connectUser.
     * Envoie un JSON de type { "email": ..., "password": ... }.
     * Renvoie un objet UserAccount en cas de succès (ou une erreur).
     */
    @POST("/connectUser")
    Call<UserAccount> connectUser(@Body LoginDto loginDto);

    /**
     * Crée un nouvel utilisateur via /createUser.
     * Envoie un JSON contenant les champs requis (RegisterDto).
     * Renvoie généralement un JSON { "status": "success", "user_id": ... }.
     * Ici on peut simplement gérer un Call<Void> ou créer un objet de réponse dédié.
     */
    @POST("/createUser")
    Call<Void> createUser(@Body RegisterDto registerDto);

    /**
     * Vous pouvez conserver /register si vous voulez un second endpoint
     * ou si votre serveur l’implémente. Sinon, vous pouvez l’enlever.
     */
    @POST("/register")
    Call<Void> register(@Body RegisterDto registerDto);

    /* ----------------------------------------------------------------------
       Gestion des commandes (ClientOrder)
       ---------------------------------------------------------------------- */

    /**
     * Crée une commande via /createOrder.
     * On envoie un objet ClientOrder (avec user_id, shop_id, items, etc.).
     * Le serveur renvoie un CreateOrderResponse
     * par exemple { "status": "success", "order_id": 123 }.
     */
    @POST("/createOrder")
    Call<CreateOrderResponse> createOrder(@Body ClientOrder clientOrder);

    /**
     * Annule une commande via /cancelOrder.
     * Même logique : on envoie { "order_id": ... } plus éventuellement email/password.
     * Vous pouvez créer un objet si vous préférez.
     */
    @POST("/cancelOrder")
    Call<Void> cancelOrder(@Body ClientOrder clientOrder);

    /**
     * Récupère le statut d’une commande via /getOrderStatus?orderId=XXX.
     * Dans votre code Flask actuel, /getOrderStatus n’est pas présent.
     * Vous pouvez l’implémenter si vous en avez besoin, ou retirer ce call.
     */
    @GET("/getOrderStatus")
    Call<OrderStatusResponse> getOrderStatus(
            @Query("orderId") int orderId
    );

    /**
     * Récupère l’historique de commandes d’un utilisateur
     * via /getOrderHistory?userId=XXX&role=client|carrier.
     * Comme /getOrderStatus, cette route n’existe pas dans votre Flask code.
     * Ajoutez-la si nécessaire, ou retirez cette méthode.
     */
    @GET("/getOrderHistory")
    Call<List<ClientOrder>> getOrderHistory(
            @Query("userId") int userId,
            @Query("role") String role
    );
}
