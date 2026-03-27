package com.bluff.repository;

import com.bluff.model.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryGameRepository implements GameRepository {
    private final ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();
    @Override
    public Game findById(String id) {
        return games.get(id);
    }
    @Override
    public void save(Game game) {
        games.put(game.getId(), game);
    }
    @Override
    public void deleteById(String id) {
        games.remove(id);
    }
    @Override
    public List<Game> findAll() {
        return new ArrayList<>(games.values());
    }
}
