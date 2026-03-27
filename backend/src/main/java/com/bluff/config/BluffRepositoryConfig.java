package com.bluff.config;

import com.bluff.repository.GameRepository;
import com.bluff.repository.InMemoryGameRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BluffRepositoryConfig {

    @Bean
    GameRepository gameRepository() {
        return new InMemoryGameRepository();
    }
}
