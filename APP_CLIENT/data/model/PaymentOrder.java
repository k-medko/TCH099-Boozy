package com.example.boozy.data.model;

import java.io.Serializable;

/**
 * PaymentOrder
 * ------------
 * Objet utilisé pour envoyer une commande avec les informations de paiement à l’API.
 * Il étend ClientOrder en y ajoutant les informations de la carte.
 */
public class PaymentOrder extends ClientOrder implements Serializable {
    private String cardName;
    private String cardNumber;
    private String cvc;
    private int expiryDateMonth;
    private int expiryDateYear;

    public PaymentOrder() {
        super();
    }

    public String getCardName() {
        return cardName;
    }
    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardNumber() {
        return cardNumber;
    }
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCvc() {
        return cvc;
    }
    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public int getExpiryDateMonth() {
        return expiryDateMonth;
    }
    public void setExpiryDateMonth(int expiryDateMonth) {
        this.expiryDateMonth = expiryDateMonth;
    }

    public int getExpiryDateYear() {
        return expiryDateYear;
    }
    public void setExpiryDateYear(int expiryDateYear) {
        this.expiryDateYear = expiryDateYear;
    }
}
