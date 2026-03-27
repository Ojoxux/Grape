package com.bluff;

import com.bluff.repository.GameRepository;
import com.bluff.service.GameService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Random;

@TestConfiguration
public class DeterministicGameServiceTestConfig {

    @Bean
    @Primary
    public GameService gameService(GameRepository gameRepository) {
        return new GameService(gameRepository, new Random(777L));
    }
}
