package controller;

import controller.DictionaryService;
import controller.GameController;
import controller.GameSessionService;
import controller.TurnTimer;
import model.GameState;
import model.Records.GamePlayer;
import model.Records.PlayerProfile;
import model.Records.WordChoice;
import model.enums.Difficulty;
import model.enums.GameMode;
import model.enums.TimerDuration;
import model.enums.WordLength;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameSessionServiceTest {
    public static void main(String[] args) {
        shouldFinishOnExactMatch();
        shouldForfeitOnTimeout();
        System.out.println("GameSessionServiceTest passed");
    }

    private static void shouldFinishOnExactMatch() {
        var timer = new TurnTimer() {
            @Override public void addListener(Listener listener) { }
            @Override public void removeListener(Listener listener) { }
            @Override public void setTimeForPlayer(GamePlayer player, int seconds) { }
            @Override public int getRemainingFor(GamePlayer player) { return 0; }
            @Override public void start(GamePlayer player) { }
            @Override public void stop() { }
            @Override public void reset() { }
        };
        var session = new GameSessionService(new GameController(new DictionaryService()), timer);
        var p1 = new GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.none, p1, p2);
        var state = session.startNewGame(cfg, new WordChoice("APPLE", model.enums.WordSource.manual), new WordChoice("GRAPE", model.enums.WordSource.manual));
        session.submitGuess("APPLE");
        if (state.getStatus() != model.enums.GameStatus.finished) {
            throw new AssertionError("Expected finished");
        }
    }

    private static void shouldForfeitOnTimeout() {
        AtomicBoolean stopped = new AtomicBoolean(false);
        var timer = new TurnTimer() {
            @Override public void addListener(Listener listener) { }
            @Override public void removeListener(Listener listener) { }
            @Override public void setTimeForPlayer(GamePlayer player, int seconds) { }
            @Override public int getRemainingFor(GamePlayer player) { return 0; }
            @Override public void start(GamePlayer player) { }
            @Override public void stop() { stopped.set(true); }
            @Override public void reset() { }
        };
        var session = new GameSessionService(new GameController(new DictionaryService()), timer);
        var p1 = new GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.oneMinute, p1, p2);
        var state = session.startNewGame(cfg, new WordChoice("APPLE", model.enums.WordSource.manual), new WordChoice("GRAPE", model.enums.WordSource.manual));
        session.onTimeExpired(p1);
        if (state.getStatus() != model.enums.GameStatus.finished) {
            throw new AssertionError("Expected finished after timeout");
        }
        if (!p2.equals(state.getWinner())) {
            throw new AssertionError("Expected opponent to win on timeout");
        }
        if (!stopped.get()) {
            throw new AssertionError("Expected timer to stop on finish");
        }
    }
}
