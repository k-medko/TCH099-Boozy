package com.example.boozy.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("user")
    private Utilisateur user;

    public String getStatus() {
        return status;
    }

    public Utilisateur getUser() {
        return user;
    }
}
