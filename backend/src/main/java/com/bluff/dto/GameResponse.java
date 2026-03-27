package com.bluff.dto;

import java.util.List;

public record GameResponse(
        String id,
        String state,
        List<PlayerResponse> players,
        String currentPlayer,
        BidResponse currentBid,
        String hostPlayerId,
        String winnerPlayerId,
        List<Integer> myDice) {}
