package com.bluff.repository;

import com.bluff.model.Game;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryGameRepositoryTest {

    @Test
    void saveThenFindByIdReturnsSameInstance() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        Game game = new Game("g1", "host1");
        repo.save(game);
        assertThat(repo.findById("g1")).isSameAs(game);
    }

    @Test
    void findByIdReturnsNullWhenMissing() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        assertThat(repo.findById("none")).isNull();
    }

    @Test
    void deleteByIdThenFindByIdReturnsNull() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        Game game = new Game("g1", "host1");
        repo.save(game);
        repo.deleteById("g1");
        assertThat(repo.findById("g1")).isNull();
    }

    @Test
    void saveOverwritesExisting() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        Game first = new Game("g1", "host1");
        Game second = new Game("g1", "host2");
        repo.save(first);
        repo.save(second);
        assertThat(repo.findById("g1")).isSameAs(second);
    }

    @Test
    void findAllContainsSavedGames() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        Game a = new Game("a", "h");
        Game b = new Game("b", "h");
        repo.save(a);
        repo.save(b);
        assertThat(repo.findAll()).containsExactlyInAnyOrder(a, b);
    }
}
