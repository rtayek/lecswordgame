package controller;

import controller.events.PlayerSlot;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

class GameSessionServiceTestCase {

    @Test
    void firstCorrectGuessInMultiplayerTriggersKnowledge() {
        var p1 = new model.GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new model.GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.none, p1, p2);
        var session = new GameSessionService(new GameController(new DictionaryService()), new NoopTimer());
        var state = session.startNewGame(cfg, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));

        session.submitGuess("GRAPE"); // p1 guessing p2's word

        assertEquals(GameStatus.awaitingWinnerKnowledge, state.getStatus(), "Expected awaitingWinnerKnowledge after first success");
        assertEquals(FinishState.finishedSuccess, state.getPlayerFinishState(p1), "Current player should be marked finishedSuccess");
        assertNull(state.getWinner(), "Winner undecided until knowledge check");
    }

    @Test
    void opponentWinsOnTimeout() {
        AtomicBoolean stopped = new AtomicBoolean(false);
        var timer = new NoopTimer() {
            @Override
            public void stop() {
                stopped.set(true);
            }
        };
        var p1 = new model.GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new model.GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.oneMinute, p1, p2);
        var session = new GameSessionService(new GameController(new DictionaryService()), timer);
        var state = session.startNewGame(cfg, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));

        session.onTimeExpired(PlayerSlot.playerOne);

        assertEquals(GameStatus.finished, state.getStatus(), "Timeout should finish the game");
        assertEquals(p2, state.getWinner(), "Opponent should win on timeout");
        assertTrue(stopped.get(), "Timer should stop on finish");
        assertEquals(FinishState.finishedFail, state.getPlayerFinishState(p1), "Expired player should be marked failed");
    }

    @Test
    void rejectsGuessesWhileAwaitingWinnerKnowledge() throws Exception {
        var p1 = new model.GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new model.GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.none, p1, p2);
        var session = new GameSessionService(new GameController(new DictionaryService()), new NoopTimer());
        var state = session.startNewGame(cfg, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));

        // Force the status to awaitingWinnerKnowledge to validate guard.
        Field statusField = GameState.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(state, GameStatus.awaitingWinnerKnowledge);

        assertThrows(IllegalStateException.class, () -> session.submitGuess("APPLE"),
                "Submitting guesses should be rejected while awaiting winner knowledge");
    }

    @Test
    void onTimeExpiredIgnoredWhenUntimed() {
        var p1 = new model.GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new model.GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.none, p1, p2);
        var session = new GameSessionService(new GameController(new DictionaryService()), new NoopTimer());
        AtomicInteger events = new AtomicInteger(0);
        session.addEventListener(new controller.events.GameEventListener() {
            @Override
            public void onGameStateEvent(controller.events.GameEventKind kind, controller.events.GameUiModel view) {
                events.incrementAndGet();
            }
            @Override
            public void onTimerEvent(controller.events.TimerView timer) {
                events.incrementAndGet();
            }
        });
        var state = session.startNewGame(cfg, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));
        assertEquals(GameStatus.inProgress, state.getStatus());

        session.onTimeExpired(PlayerSlot.playerOne);

        assertEquals(GameStatus.inProgress, state.getStatus(), "Untimed games should ignore timeouts");
        assertEquals(0, events.get(), "No events should be published for untimed expiration");
    }

    @Test
    void onTimeUpdatedIgnoredWhenUntimed() {
        var p1 = new model.GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new model.GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.none, p1, p2);
        var session = new GameSessionService(new GameController(new DictionaryService()), new NoopTimer());
        AtomicInteger events = new AtomicInteger(0);
        session.addEventListener(new controller.events.GameEventListener() {
            @Override
            public void onGameStateEvent(controller.events.GameEventKind kind, controller.events.GameUiModel view) {
                events.incrementAndGet();
            }
            @Override
            public void onTimerEvent(controller.events.TimerView timer) {
                events.incrementAndGet();
            }
        });
        session.startNewGame(cfg, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));

        session.onTimeUpdated(PlayerSlot.playerOne, 10);

        assertEquals(0, events.get(), "Untimed games should ignore timer updates");
    }

    @Test
    void winnerKnowledgeFlowIsEventDriven() {
        var p1 = new model.GamePlayer(new PlayerProfile("P1", ""), true);
        var p2 = new model.GamePlayer(new PlayerProfile("P2", ""), true);
        var cfg = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.none, p1, p2);
        var session = new GameSessionService(new GameController(new DictionaryService()), new NoopTimer());
        AtomicInteger finishedEvents = new AtomicInteger(0);
        session.addEventListener(new controller.events.GameEventListener() {
            @Override
            public void onGameStateEvent(controller.events.GameEventKind kind, controller.events.GameUiModel view) {
                if (kind == controller.events.GameEventKind.gameFinished) {
                    finishedEvents.incrementAndGet();
                }
            }
            @Override public void onTimerEvent(controller.events.TimerView timer) { }
        });
        var state = session.startNewGame(cfg, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));
        // First correct guess puts us into awaitingWinnerKnowledge
        session.submitGuess("GRAPE");
        assertEquals(GameStatus.awaitingWinnerKnowledge, state.getStatus());

        session.applyWinnerKnowledge(true);

        assertEquals(GameStatus.finished, state.getStatus(), "Winner knowledge should finish the game");
        assertEquals(1, finishedEvents.get(), "Outcome should be delivered via event");
        assertTrue(state.getWinnerKnewWord(), "Winner knowledge should be recorded");
    }

    private static class NoopTimer implements TurnTimer {
        @Override public void addListener(Listener listener) { }
        @Override public void removeListener(Listener listener) { }
        @Override public void setTimeForPlayer(PlayerSlot slot, int seconds) { }
        @Override public int getRemainingFor(PlayerSlot slot) { return 0; }
        @Override public void start(PlayerSlot slot) { }
        @Override public void stop() { }
        @Override public void reset() { }
    }
}
