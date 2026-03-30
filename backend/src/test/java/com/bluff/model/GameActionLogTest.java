package com.bluff.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GameActionLogTest {

    @Test
    void start_clearsActionLog_andSetsCurrentRoundOne() {
        Game g = new Game("g1", "h1");
        g.join("h1", "Host");
        g.join("p2", "Bob");
        g.getActionLog().add(TurnLogEntry.bid(99, "x", "y", 1, 1));

        g.start("h1");

        assertThat(g.getCurrentRound()).isEqualTo(1);
        assertThat(g.getActionLog()).isEmpty();
    }

    @Test
    void bid_appendsBidEntry() {
        Game g = newGameTwoPlayers();
        g.start("h1");
        g.bid("h1", 2, 3);

        assertThat(g.getActionLog()).hasSize(1);
        TurnLogEntry e = g.getActionLog().get(0);
        assertThat(e.getType()).isEqualTo(TurnLogEntry.TYPE_BID);
        assertThat(e.getRound()).isEqualTo(1);
        assertThat(e.getPlayerId()).isEqualTo("h1");
        assertThat(e.getPlayerName()).isEqualTo("Host");
        assertThat(e.getQuantity()).isEqualTo(2);
        assertThat(e.getFace()).isEqualTo(3);
        assertThat(e.getActualCount()).isNull();
        assertThat(e.getChallengeResult()).isNull();
        assertThat(e.getPenaltyDescription()).isNull();
    }

    @Test
    void multiple_bids_accumulateInActionLog() {
        Game g = newGameTwoPlayers();
        g.start("h1");
        g.bid("h1", 1, 1);
        g.bid("p2", 2, 1);

        assertThat(g.getActionLog()).hasSize(2);
        TurnLogEntry first = g.getActionLog().get(0);
        TurnLogEntry second = g.getActionLog().get(1);
        assertThat(first.getType()).isEqualTo(TurnLogEntry.TYPE_BID);
        assertThat(first.getRound()).isEqualTo(1);
        assertThat(first.getPlayerId()).isEqualTo("h1");
        assertThat(first.getPlayerName()).isEqualTo("Host");
        assertThat(first.getQuantity()).isEqualTo(1);
        assertThat(first.getFace()).isEqualTo(1);
        assertThat(second.getType()).isEqualTo(TurnLogEntry.TYPE_BID);
        assertThat(second.getRound()).isEqualTo(1);
        assertThat(second.getPlayerId()).isEqualTo("p2");
        assertThat(second.getPlayerName()).isEqualTo("Bob");
        assertThat(second.getQuantity()).isEqualTo(2);
        assertThat(second.getFace()).isEqualTo(1);
    }

    @Test
    void challenge_bidderLoses_logsBidderPenalty() {
        Game g = newGameTwoPlayers();
        g.start("h1");
        // h1 bids first; face=1, table has no 1 or 6 -> A=0
        setAllDice(g, Arrays.asList(2, 2, 2, 2, 2), Arrays.asList(2, 2, 2, 2, 2));
        g.bid("h1", 3, 1);
        g.challenge("p2");

        TurnLogEntry ch = lastChallengeEntry(g);
        assertThat(ch.getRound()).isEqualTo(1);
        assertThat(ch.getQuantity()).isNull();
        assertThat(ch.getFace()).isNull();
        assertThat(ch.getChallengeResult()).isEqualTo("BIDDER_LOSES");
        assertThat(ch.getActualCount()).isEqualTo(0);
        assertThat(ch.getPenaltyDescription()).isEqualTo("Host がダイス3個失う");
        assertThat(ch.getPlayerId()).isEqualTo("p2");
        assertThat(ch.getPlayerName()).isEqualTo("Bob");
    }

    @Test
    void challenge_challengerLoses_logsChallengerPenalty() {
        Game g = newGameTwoPlayers();
        g.start("h1");
        // All 1s -> A=10 for face 1 (each die counts as 1 or 6 for face 1... 1 counts)
        setAllDice(g, Arrays.asList(1, 1, 1, 1, 1), Arrays.asList(1, 1, 1, 1, 1));
        g.bid("h1", 1, 1);
        g.challenge("p2");

        TurnLogEntry ch = lastChallengeEntry(g);
        assertThat(ch.getRound()).isEqualTo(1);
        assertThat(ch.getChallengeResult()).isEqualTo("CHALLENGER_LOSES");
        assertThat(ch.getActualCount()).isEqualTo(10);
        assertThat(ch.getPenaltyDescription()).isEqualTo("Bob がダイス9個失う");
    }

    @Test
    void challenge_exactMatch_logsExactMatch() {
        Game g = newGameTwoPlayers();
        g.start("h1");
        // Exactly two 1s or 6s contributing to face 1: use two 1s, rest non-wild for face 1
        setAllDice(g, Arrays.asList(1, 1, 2, 2, 2), Arrays.asList(2, 2, 2, 2, 2));
        g.bid("h1", 2, 1);
        g.challenge("p2");

        TurnLogEntry ch = lastChallengeEntry(g);
        assertThat(ch.getRound()).isEqualTo(1);
        assertThat(ch.getChallengeResult()).isEqualTo("EXACT_MATCH");
        assertThat(ch.getActualCount()).isEqualTo(2);
        assertThat(ch.getPenaltyDescription()).isEqualTo("宣言者以外の全員がダイス1個ずつ失う");
    }

    @Test
    void resolveRound_whenContinuing_incrementsRound_andAddsRoundStart() {
        Game g = newGameTwoPlayers();
        g.start("h1");
        setAllDice(g, Arrays.asList(2, 2, 2, 2, 2), Arrays.asList(2, 2, 2, 2, 2));
        g.bid("h1", 3, 1);
        g.challenge("p2");

        assertThat(g.getCurrentRound()).isEqualTo(2);
        TurnLogEntry last = g.getActionLog().get(g.getActionLog().size() - 1);
        assertThat(last.getType()).isEqualTo(TurnLogEntry.TYPE_ROUND_START);
        assertThat(last.getRound()).isEqualTo(2);
        assertThat(last.getPlayerId()).isEqualTo("h1");
        assertThat(last.getPlayerName()).isEqualTo("Host");
        assertThat(g.getState()).isEqualTo(GameState.PLAYING);
    }

    @Test
    void resolveRound_whenFinished_doesNotAddRoundStart() {
        Game g = new Game("g1", "h1");
        g.join("h1", "Host");
        g.join("p2", "Bob");
        g.start("h1");
        // One challenger left with dice — stack so challenge ends game (one survivor)
        setAllDice(g, Arrays.asList(1, 1, 1, 1, 1), Arrays.asList(1));
        g.bid("h1", 1, 1);
        g.challenge("p2");

        assertThat(g.getState()).isEqualTo(GameState.FINISHED);
        assertThat(g.getActionLog().stream().noneMatch(e -> TurnLogEntry.TYPE_ROUND_START.equals(e.getType())))
                .isTrue();
        assertThat(g.getCurrentRound()).isEqualTo(1);
    }

    private static Game newGameTwoPlayers() {
        Game g = new Game("g1", "h1");
        g.join("h1", "Host");
        g.join("p2", "Bob");
        return g;
    }

    private static void setAllDice(Game g, List<Integer> p0, List<Integer> p1) {
        g.getPlayers().get(0).setDice(new java.util.ArrayList<>(p0));
        g.getPlayers().get(1).setDice(new java.util.ArrayList<>(p1));
    }

    private static TurnLogEntry lastChallengeEntry(Game g) {
        return g.getActionLog().stream()
                .filter(e -> TurnLogEntry.TYPE_CHALLENGE.equals(e.getType()))
                .reduce((a, b) -> b)
                .orElseThrow();
    }
}
