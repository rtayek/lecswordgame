package controller;

import java.util.concurrent.atomic.AtomicBoolean;

import model.GamePlayer;
import model.GameState;
import model.PlayerProfile;
import model.WordChoice;
import model.enums.Difficulty;
import model.enums.FinishState;
import model.enums.GameMode;
import model.enums.GameStatus;
import model.enums.TimerDuration;
import model.enums.WordLength;
import model.enums.WordSource;

/**
 * Lightweight self-checks without JUnit dependency.
 */
public final class GameSessionServiceTest {

    public static void main(String[] args) {
        firstCorrectGuessInMultiplayerTriggersKnowledge();
        opponentWinsOnTimeout();
        System.out.println("GameSessionServiceTest passed");
    }

    private static void firstCorrectGuessInMultiplayerTriggersKnowledge() {
        var p1 = new GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.none, p1, p2);
        var session = new GameSessionService(new GameController(new DictionaryService()), new NoopTimer());
        var state = session.startNewGame(cfg, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));

        session.submitGuess("GRAPE"); // p1 guessing p2's word

        if (state.getStatus() != GameStatus.awaitingWinnerKnowledge) {
            throw new AssertionError("Expected awaitingWinnerKnowledge after first success");
        }
        if (state.getPlayerFinishState(p1) != FinishState.finishedSuccess) {
            throw new AssertionError("Current player should be marked finishedSuccess");
        }
        if (state.getWinner() != null) {
            throw new AssertionError("Winner undecided until knowledge check");
        }
    }

    private static void opponentWinsOnTimeout() {
        AtomicBoolean stopped = new AtomicBoolean(false);
        var timer = new NoopTimer() {
            @Override
            public void stop() {
                stopped.set(true);
            }
        };
        var p1 = new GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.oneMinute, p1, p2);
        var session = new GameSessionService(new GameController(new DictionaryService()), timer);
        var state = session.startNewGame(cfg, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));

        session.onTimeExpired(p1);

        if (state.getStatus() != GameStatus.finished) {
            throw new AssertionError("Timeout should finish the game");
        }
        if (state.getWinner() != p2) {
            throw new AssertionError("Opponent should win on timeout");
        }
        if (!stopped.get()) {
            throw new AssertionError("Timer should stop on finish");
        }
        if (state.getPlayerFinishState(p1) != FinishState.finishedFail) {
            throw new AssertionError("Expired player should be marked failed");
        }
    }

    private static class NoopTimer implements TurnTimer {
        @Override public void addListener(Listener listener) { }
        @Override public void removeListener(Listener listener) { }
        @Override public void setTimeForPlayer(GamePlayer player, int seconds) { }
        @Override public int getRemainingFor(GamePlayer player) { return 0; }
        @Override public void start(GamePlayer player) { }
        @Override public void stop() { }
        @Override public void reset() { }
    }
}
