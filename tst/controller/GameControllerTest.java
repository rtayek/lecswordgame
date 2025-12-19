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

/**
 * Lightweight self-checks without JUnit dependency.
 * Run with: java -cp bin;src;tst controller.GameControllerTest
 */
public final class GameControllerTest {

    public static void main(String[] args) {
        shouldWinSoloOnExactMatch();
        shouldStayInProgressOnWrongGuess();
        System.out.println("GameControllerTest passed");
    }

    private static void shouldWinSoloOnExactMatch() {
        var gameController = new GameController(new DictionaryService());
        var player = new GamePlayer(new PlayerProfile("P1", ""), true);
        var cpu = new GamePlayer(new PlayerProfile("CPU", ""), false);
        var config = new GameState.GameConfig(GameMode.solo, Difficulty.normal, WordLength.five, TimerDuration.none, player, cpu);

        var state = gameController.startNewGame(config, null, new WordChoice("APPLE", WordSource.manual));
        gameController.submitGuess(state, player, "apple");

        if (state.getStatus() != GameStatus.finished) {
            throw new AssertionError("Game should finish on exact match");
        }
        if (state.getWinner() != player) {
            throw new AssertionError("Human should be winner");
        }
    }

    private static void shouldStayInProgressOnWrongGuess() {
        var gameController = new GameController(new DictionaryService());
        var player = new GamePlayer(new PlayerProfile("P1", ""), true);
        var cpu = new GamePlayer(new PlayerProfile("CPU", ""), false);
        var config = new GameState.GameConfig(GameMode.solo, Difficulty.normal, WordLength.five, TimerDuration.none, player, cpu);

        var state = gameController.startNewGame(config, null, new WordChoice("APPLE", WordSource.manual));
        gameController.submitGuess(state, player, "grape");

        if (state.getStatus() != GameStatus.inProgress) {
            throw new AssertionError("Wrong guess should keep game in progress");
        }
        if (state.getWinner() != null) {
            throw new AssertionError("No winner on wrong guess");
        }
    }
}
