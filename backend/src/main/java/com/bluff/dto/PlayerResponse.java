package com.bluff.dto;

public record PlayerResponse(
        String id, String name, boolean cpu, int diceCount, boolean eliminated) {}
