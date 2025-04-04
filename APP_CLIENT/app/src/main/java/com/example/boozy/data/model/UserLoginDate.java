package com.example.boozy.data.model;

public class UserLoginData {
    private String email;
    private String password;

    public UserLoginData(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}

