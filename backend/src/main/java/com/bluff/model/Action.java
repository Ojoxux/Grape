package com.bluff.model;

public class Action {
    private ActionType type;
    private String playerId;
    private Integer quantity;   // サイコロの個数
    private Integer face;       // サイコロの面

    public Action(ActionType type, String playerId, Integer quantity, Integer face) {
        this.type = type;
        this.playerId = playerId;
        this.quantity = quantity;
        this.face = face;
    }

    public ActionType getType() {
        return type;
    }

    public String getPlayerId() {
        return playerId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getFace() {
        return face;
    }
}
