package com.bluff.service;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String gameId) {
        super("ゲームが見つかりません: " + gameId);
    }
}
