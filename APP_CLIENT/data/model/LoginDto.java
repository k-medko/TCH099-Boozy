package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * LoginDto
 * --------
 * Objet envoyé à /connectUser (ou /login) pour se connecter.
 */
public class LoginDto implements Serializable {
    private String email;
    private String password;

    public LoginDto() { }

    public LoginDto(String email, String password) {
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
