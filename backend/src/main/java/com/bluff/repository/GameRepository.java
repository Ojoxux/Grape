package com.bluff.repository;

import com.bluff.model.Game;

import java.util.List;

public interface GameRepository {
    Game findById(String id);
    void save(Game game);
    void deleteById(String id);
    List<Game> findAll();
}
