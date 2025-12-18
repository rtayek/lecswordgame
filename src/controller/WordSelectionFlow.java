package controller;

import model.enums.GameMode;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.WordChoice;

/**
 * Manages the pre-game word selection flow and yields a start request when ready.
 */
public class WordSelectionFlow {

    public record StartRequest(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo,
                               WordChoice playerOneWord, WordChoice playerTwoWord) { }

    private GameConfig pendingConfig;
    private GamePlayer pendingPlayerOne;
    private GamePlayer pendingPlayerTwo;
    private WordChoice playerOneChosenWord;

    public void start(GameConfig config) {
        this.pendingConfig = config;
        this.pendingPlayerOne = config.playerOne();
        this.pendingPlayerTwo = config.playerTwo();
        this.playerOneChosenWord = null;
    }

    public GameConfig getPendingConfig() {
        return pendingConfig;
    }

    public GamePlayer getPendingPlayerOne() {
        return pendingPlayerOne;
    }

    public GamePlayer getPendingPlayerTwo() {
        return pendingPlayerTwo;
    }

    public StartRequest playerOneSelected(WordChoice choice) {
        ensureConfig();
        if (pendingConfig.mode() == GameMode.solo) {
            // In solo, player one is the guesser; choice sets player two's word.
            return new StartRequest(pendingConfig, pendingPlayerOne, pendingPlayerTwo, null, choice);
        }
        this.playerOneChosenWord = choice;
        return null; // need player two
    }

    public StartRequest playerTwoSelected(WordChoice choice) {
        ensureConfig();
        if (playerOneChosenWord == null) {
            throw new IllegalStateException("Player one word must be selected first.");
        }
        return new StartRequest(pendingConfig, pendingPlayerOne, pendingPlayerTwo, playerOneChosenWord, choice);
    }

    public void clear() {
        pendingConfig = null;
        pendingPlayerOne = null;
        pendingPlayerTwo = null;
        playerOneChosenWord = null;
    }

    private void ensureConfig() {
        if (pendingConfig == null) {
            throw new IllegalStateException("No pending game configuration.");
        }
    }
}
