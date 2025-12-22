package controller;

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
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameControllerTestCase {

    @Test
    void shouldWinSoloOnExactMatch() {
        var gameController = new GameController(new DictionaryService());
        var player = new GamePlayer(new PlayerProfile("P1", ""), true);
        var cpu = new GamePlayer(new PlayerProfile("CPU", ""), false);
        var config = new GameState.GameConfig(GameMode.solo, Difficulty.normal, WordLength.five, TimerDuration.none, player, cpu);

        var state = gameController.startNewGame(config, null, new WordChoice("APPLE", WordSource.manual));
        gameController.submitGuess(state, player, "apple");

        assertEquals(GameStatus.finished, state.getStatus(), "Game should finish on exact match");
        assertEquals(player, state.getWinner(), "Human should be winner");
    }

    @Test
    void shouldStayInProgressOnWrongGuess() {
        var gameController = new GameController(new DictionaryService());
        var player = new GamePlayer(new PlayerProfile("P1", ""), true);
        var cpu = new GamePlayer(new PlayerProfile("CPU", ""), false);
        var config = new GameState.GameConfig(GameMode.solo, Difficulty.normal, WordLength.five, TimerDuration.none, player, cpu);

        var state = gameController.startNewGame(config, null, new WordChoice("APPLE", WordSource.manual));
        gameController.submitGuess(state, player, "grape");

        assertEquals(GameStatus.inProgress, state.getStatus(), "Wrong guess should keep game in progress");
        assertNull(state.getWinner(), "No winner on wrong guess");
    }
}
