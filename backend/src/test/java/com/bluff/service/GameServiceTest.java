package com.bluff.service;

import com.bluff.model.Action;
import com.bluff.model.ActionType;
import com.bluff.model.Bid;
import com.bluff.model.Game;
import com.bluff.model.GameState;
import com.bluff.repository.InMemoryGameRepository;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameServiceTest {

    @Test
    void createGame_rejectsCpuCountOutOfRange() {
        GameService svc = new GameService(new InMemoryGameRepository());
        assertThatThrownBy(() -> svc.createGame("p", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> svc.createGame("p", 6)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getGame_throwsWhenMissing() {
        GameService svc = new GameService(new InMemoryGameRepository());
        assertThatThrownBy(() -> svc.getGame("no-such-id", null)).isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void performAction_rejectsJoinAndStart() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        GameService svc = new GameService(repo);
        var created = svc.createGame("p", 1);
        Action join = new Action(ActionType.JOIN, created.playerId(), null, null);
        assertThatThrownBy(() -> svc.performAction(created.gameId(), join))
                .isInstanceOf(IllegalArgumentException.class);
        Action start = new Action(ActionType.START, created.playerId(), null, null);
        assertThatThrownBy(() -> svc.performAction(created.gameId(), start))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void finishedGame_staysInRepository() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        GameService svc = new GameService(repo);
        var created = svc.createGame("p", 1);
        Game game = repo.findById(created.gameId());
        game.setState(GameState.FINISHED);
        repo.save(game);
        assertThat(repo.findById(created.gameId())).isNotNull();
    }

    @Test
    void getGame_includesActionLogAfterStartAndBid() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        GameService svc = new GameService(repo, deterministicNoChallenge());
        var created = svc.createGame("me", 1);
        svc.startGame(created.gameId(), created.playerId());
        Game game = repo.findById(created.gameId());
        Bid cur = game.getCurrentBid();
        assertThat(cur).isNotNull();
        int[] legal = nextLegalBidForHuman(cur, created.playerId());
        svc.performAction(
                created.gameId(),
                new Action(ActionType.BID, created.playerId(), legal[0], legal[1]));
        GameService.GameDetailView view = svc.getGame(created.gameId(), created.playerId());
        assertThat(view.actionLog()).isNotEmpty();
        assertThat(view.actionLog().stream().anyMatch(e -> "BID".equals(e.type()) && created.playerId().equals(e.playerId())))
                .isTrue();
    }

    @Test
    void startGame_runsCpuUntilHumanTurn() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        Random noRandomChallenge =
                new Random() {
                    @Override
                    public double nextDouble() {
                        return 0.99;
                    }
                };
        GameService svc = new GameService(repo, noRandomChallenge);
        var created = svc.createGame("me", 1);
        svc.startGame(created.gameId(), created.playerId());
        GameService.GameDetailView view = svc.getGame(created.gameId(), created.playerId());
        assertThat(view.currentPlayerId()).isEqualTo(created.playerId());
        assertThat(view.currentBid()).isNotNull();
        assertThat(view.myDice()).hasSize(5);
    }

    @Test
    void listGames_returnsCreatedGames() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        GameService svc = new GameService(repo);
        var a = svc.createGame("a", 2);
        var b = svc.createGame("b", 1);
        assertThat(svc.listGames()).hasSize(2);
        assertThat(svc.listGames()).extracting(GameService.GameListItem::id).contains(a.gameId(), b.gameId());
    }

    @Test
    void getGame_returnsMyDiceForViewerPlayerId() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        GameService svc = new GameService(repo, deterministicNoChallenge());
        var created = svc.createGame("me", 1);
        svc.startGame(created.gameId(), created.playerId());
        Game game = repo.findById(created.gameId());
        String cpuId = game.getPlayers().get(0).getId();
        GameService.GameDetailView fromCpuPerspective = svc.getGame(created.gameId(), cpuId);
        assertThat(fromCpuPerspective.myDice()).hasSize(5);
    }

    @Test
    void joinGame_addsPlayerAndReturnsId() {
        InMemoryGameRepository repo = new InMemoryGameRepository();
        GameService svc = new GameService(repo);
        var created = svc.createGame("host", 1);
        String guestId = svc.joinGame(created.gameId(), "guest");
        GameService.GameDetailView view = svc.getGame(created.gameId(), guestId);
        assertThat(view.players().stream().map(GameService.PlayerSummary::id).anyMatch(guestId::equals))
                .isTrue();
    }

    private static Random deterministicNoChallenge() {
        return new Random() {
            @Override
            public double nextDouble() {
                return 0.99;
            }
        };
    }

    private static int[] nextLegalBidForHuman(Bid currentBid, String humanPlayerId) {
        int q = currentBid.getQuantity();
        int f = currentBid.getFace();
        Bid prev = new Bid(q, f, currentBid.getPlayerId());
        for (int nq = 1; nq <= q + 60; nq++) {
            for (int nf = 1; nf <= 6; nf++) {
                if (Game.isValidBidAfter(prev, new Bid(nq, nf, humanPlayerId))) {
                    return new int[] {nq, nf};
                }
            }
        }
        throw new IllegalStateException("no legal bid found");
    }
}
