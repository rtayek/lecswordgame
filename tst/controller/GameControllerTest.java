package controller;

import controller.DictionaryService;
import controller.GameController;
import model.Enums.Difficulty;
import model.Enums.GameMode;
import model.Enums.GameStatus;
import model.Enums.TimerDuration;
import model.Enums.WordLength;
import model.Enums.WordSource;
import model.GameState;
import model.Records.GamePlayer;
import model.Records.PlayerProfile;
import model.Records.WordChoice;

/**
 * Minimal smoke tests for GameController submitGuess logic.
 * Run with:
 *   javac -d bin-test -cp bin;src @(Get-ChildItem -Path tst/controller -Filter *.java | % FullName)
 *   java -cp bin;bin-test tst.controller.GameControllerTest
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

        var state = gameController.startNewGame(config, new WordChoice("APPLE", WordSource.manual), null);
        gameController.submitGuess(state, player, "apple");

        if (state.getStatus() != GameStatus.finished) {
            throw new AssertionError("Expected finished after exact match, got " + state.getStatus());
        }
        if (!player.equals(state.getWinner())) {
            throw new AssertionError("Expected winner to be player");
        }
    }

    private static void shouldStayInProgressOnWrongGuess() {
        var gameController = new GameController(new DictionaryService());

        var player = new GamePlayer(new PlayerProfile("P1", ""), true);
        var cpu = new GamePlayer(new PlayerProfile("CPU", ""), false);
        var config = new GameState.GameConfig(GameMode.solo, Difficulty.normal, WordLength.five, TimerDuration.none, player, cpu);

        var state = gameController.startNewGame(config, new WordChoice("APPLE", WordSource.manual), null);
        gameController.submitGuess(state, player, "GRAPE");

        if (state.getStatus() == GameStatus.finished) {
            throw new AssertionError("Game should not be finished on wrong guess in solo mode");
        }
        if (state.getWinner() != null) {
            throw new AssertionError("No winner expected yet");
        }
    }
}
