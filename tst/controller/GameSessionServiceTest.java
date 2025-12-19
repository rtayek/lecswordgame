package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.GamePlayer;
import model.GameState;
import model.PlayerProfile;
import model.WordChoice;
import model.enums.Difficulty;
import model.enums.GameMode;
import model.enums.GameStatus;
import model.enums.FinishState;
import model.enums.TimerDuration;
import model.enums.WordLength;
import model.enums.WordSource;

class GameSessionServiceTest {

    private GamePlayer p1;
    private GamePlayer p2;
    private GameState.GameConfig multiplayerConfig;

    @BeforeEach
    void setUp() {
        p1 = new GamePlayer(new PlayerProfile("P1", ""), true);
        p2 = new GamePlayer(new PlayerProfile("P2", ""), true);
        multiplayerConfig = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.none, p1, p2);
    }

    @Test
    void firstCorrectGuessInMultiplayerTriggersFinalChance() {
        var session = new GameSessionService(new GameController(new DictionaryService()), new NoopTimer());
        var state = session.startNewGame(multiplayerConfig, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));

        // Player 1 starts; guessing opponent's word ("GRAPE") should mark success and await final chance.
        session.submitGuess("GRAPE");

        assertEquals(GameStatus.awaitingWinnerKnowledge, state.getStatus(), "First success should await knowledge check");
        assertEquals(FinishState.finishedSuccess, state.getPlayerFinishState(p1), "Current player marked finished");
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
        var session = new GameSessionService(new GameController(new DictionaryService()), timer);
        var timedConfig = new GameState.GameConfig(GameMode.multiplayer, Difficulty.normal, WordLength.five, TimerDuration.oneMinute, p1, p2);
        var state = session.startNewGame(timedConfig, new WordChoice("APPLE", WordSource.manual), new WordChoice("GRAPE", WordSource.manual));

        session.onTimeExpired(p1);

        assertEquals(GameStatus.finished, state.getStatus(), "Timeout should finish the game");
        assertEquals(p2, state.getWinner(), "Opponent should win on timeout");
        assertTrue(stopped.get(), "Timer should stop on finish");
        assertEquals(FinishState.finishedFail, state.getPlayerFinishState(p1), "Expired player should be marked failed");
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
