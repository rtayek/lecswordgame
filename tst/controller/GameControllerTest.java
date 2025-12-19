package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.GamePlayer;
import model.GameState;
import model.PlayerProfile;
import model.WordChoice;
import model.enums.Difficulty;
import model.enums.GameMode;
import model.enums.GameStatus;
import model.enums.TimerDuration;
import model.enums.WordLength;
import model.enums.WordSource;

/**
 * JUnit 5 smoke tests for GameController submitGuess logic.
 */
class GameControllerTest {

    private GameController gameController;
    private GamePlayer player;
    private GamePlayer cpu;
    private GameState.GameConfig baseConfig;

    @BeforeEach
    void setUp() {
        gameController = new GameController(new DictionaryService());
        player = new GamePlayer(new PlayerProfile("P1", ""), true);
        cpu = new GamePlayer(new PlayerProfile("CPU", ""), false);
        baseConfig = new GameState.GameConfig(GameMode.solo, Difficulty.normal, WordLength.five, TimerDuration.none, player, cpu);
    }

    @Test
    void shouldWinSoloOnExactMatch() {
        var state = gameController.startNewGame(baseConfig, null, new WordChoice("APPLE", WordSource.manual));
        gameController.submitGuess(state, player, "apple");

        assertEquals(GameStatus.finished, state.getStatus(), "Game should finish on exact match");
        assertSame(player, state.getWinner(), "Human should be winner");
    }

    @Test
    void shouldStayInProgressOnWrongGuess() {
        var state = gameController.startNewGame(baseConfig, null, new WordChoice("APPLE", WordSource.manual));
        gameController.submitGuess(state, player, "grape");

        assertEquals(GameStatus.inProgress, state.getStatus(), "Wrong guess should keep game in progress");
        assertNull(state.getWinner(), "No winner on wrong guess");
    }
}
