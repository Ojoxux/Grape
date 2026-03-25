package com.bluff.model;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private String id;
    private GameState state;
    private List<Player> players;
    private int currentPlayerIndex;
    private Bid currentBid;
    private String lastBidPlayerId;
    private String hostPlayerId;
    private String pendingOpeningPlayerId;

    public Game(String id, String hostPlayerId) {
        this.id = id;
        this.state = GameState.WAITING;
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.currentBid = null;
        this.lastBidPlayerId = null;
        this.hostPlayerId = hostPlayerId;
        this.pendingOpeningPlayerId = null;
    }

    public String getId() {
        return id;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public Bid getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(Bid currentBid) {
        this.currentBid = currentBid;
    }

    public String getLastBidPlayerId() {
        return lastBidPlayerId;
    }

    public void setLastBidPlayerId(String lastBidPlayerId) {
        this.lastBidPlayerId = lastBidPlayerId;
    }

    public String getHostPlayerId() {
        return hostPlayerId;
    }

    public String getPendingOpeningPlayerId() {
        return pendingOpeningPlayerId;
    }

    public void setPendingOpeningPlayerId(String pendingOpeningPlayerId) {
        this.pendingOpeningPlayerId = pendingOpeningPlayerId;
    }
}
