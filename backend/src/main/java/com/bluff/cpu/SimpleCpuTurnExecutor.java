package com.bluff.cpu;

import com.bluff.model.Bid;
import com.bluff.model.Game;
import com.bluff.model.Player;

import java.util.Random;

public final class SimpleCpuTurnExecutor {

    private SimpleCpuTurnExecutor() {}

    public static void executeTurn(Game game, Random random) {
        Player current = game.getPlayers().get(game.getCurrentPlayerIndex());
        if (!current.isCpu()) {
            throw new IllegalStateException("現在のプレイヤーはCPUではありません");
        }
        String playerId = current.getId();
        if (game.getCurrentBid() == null) {
            game.bid(playerId, 1, 1);
            return;
        }
        if (random.nextDouble() < 0.2) {
            game.challenge(playerId);
            return;
        }
        Bid prev = game.getCurrentBid();
        int maxQ = totalDiceOnTable(game) + 8;
        for (int q = 1; q <= maxQ; q++) {
            for (int f = 1; f <= 6; f++) {
                Bid next = new Bid(q, f, playerId);
                if (Game.isValidBidAfter(prev, next)) {
                    game.bid(playerId, q, f);
                    return;
                }
            }
        }
        game.challenge(playerId);
    }

    private static int totalDiceOnTable(Game game) {
        int n = 0;
        for (Player p : game.getPlayers()) {
            if (!p.isEliminated()) {
                n += p.getDice().size();
            }
        }
        return Math.max(n, 1);
    }
}
