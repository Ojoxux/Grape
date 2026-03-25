package com.bluff.model;

public class Bid {
    private int quantity;   // サイコロの個数
    private int face;       // サイコロの面
    private String playerId;

    public Bid(int quantity, int face, String playerId) {
        this.quantity = quantity;
        this.face = face;
        this.playerId = playerId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getFace() {
        return face;
    }

    public String getPlayerId() {
        return playerId;
    }
}
