package controller;

import controller.events.PlayerSlot;
import java.util.concurrent.atomic.AtomicBoolean;
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
