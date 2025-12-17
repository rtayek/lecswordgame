package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import model.Enums.Difficulty;
import model.Enums.FinishState;
import model.Enums.GameMode;
import model.Enums.GameStatus;
import model.Enums.TimerDuration;
import model.Enums.WordLength;
import model.Records.GamePlayer;
import model.Records.GuessEntry;
import model.Records.WordChoice;
import model.Records.GuessResult;

/**
 * Represents the live, mutable state of a game in progress.
 * It holds a reference to the immutable GameConfig and manages data that changes on each turn.
 */
public class GameState {
    public static record GameConfig(
            GameMode mode,
            Difficulty difficulty,
            WordLength wordLength,
            TimerDuration timerDuration,
            GamePlayer playerOne,
            GamePlayer playerTwo
        ) {
        public static GameConfig withDefaults(GameMode mode, GamePlayer playerOne, GamePlayer playerTwo) {
            return new GameConfig(mode, Difficulty.normal, WordLength.five, TimerDuration.none, playerOne, playerTwo);
        }
    }

    public GameState(GameConfig config) {
        this.id = UUID.randomUUID().toString();
        this.config = config;
        this.status = GameStatus.setup;
        this.currentTurn = config.playerOne(); // Default to player one starting
        this.guesses = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public GameConfig getConfig() {
        return config;
    }


    // --- Live State Management ---

    public WordChoice getPlayerOneWord() {
        return playerOneWord;
    }

    public WordChoice getPlayerTwoWord() {
        return playerTwoWord;
    }

    public GameStatus getStatus() {
        return status;
    }

    public GamePlayer getCurrentTurn() {
        return currentTurn;
    }

    public List<GuessEntry> getGuesses() {
        return new ArrayList<>(guesses);
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public void setPlayerOneWord(WordChoice playerOneWord) {
        validateWordLength(playerOneWord);
        this.playerOneWord = playerOneWord;
    }

    public void setPlayerTwoWord(WordChoice playerTwoWord) {
        validateWordLength(playerTwoWord);
        this.playerTwoWord = playerTwoWord;
    }

    public void switchTurn() {
        if (config.playerOne() != null && config.playerTwo() != null) {
            currentTurn = Objects.equals(currentTurn, config.playerOne()) ? config.playerTwo() : config.playerOne();
        }
    }

    public GamePlayer getWinner() {
        return winner;
    }

    public void setWinner(GamePlayer winner) {
        this.winner = winner;
    }

    public void addGuess(GuessEntry entry) {
        guesses.add(entry);
    }

    public WordChoice wordFor(GamePlayer player) {
        if (player == null) {
            return null;
        }
        var isPlayerOne = Objects.equals(player, config.playerOne());
        return isPlayerOne ? playerTwoWord : playerOneWord;
    }

    public GamePlayer getOpponent(GamePlayer player) {
        if (player == null) return null;
        return Objects.equals(player, config.playerOne()) ? config.playerTwo() : config.playerOne();
    }

    public void setPlayerFinishState(GamePlayer player, FinishState state) {
        if (player == null) return;
        if (Objects.equals(player, config.playerOne())) {
            this.playerOneFinishState = state;
        } else {
            this.playerTwoFinishState = state;
        }
    }

    public FinishState getPlayerFinishState(GamePlayer player) {
        if (player == null) return FinishState.NOT_FINISHED;
        return Objects.equals(player, config.playerOne()) ? playerOneFinishState : playerTwoFinishState;
    }

    public void applyGuessResult(GamePlayer player, GuessResult result) {
        if (result == null) {
            return;
        }

        if (config.mode() == GameMode.solo) {
            if (result.exactMatch()) {
                setWinner(player);
                setStatus(GameStatus.finished);
            }
            return;
        }

        GamePlayer opponent = getOpponent(player);
        boolean isFinalGuess = getPlayerFinishState(opponent) != FinishState.NOT_FINISHED;

        if (result.exactMatch()) {
            setPlayerFinishState(player, FinishState.FINISHED_SUCCESS);
            if (isFinalGuess) {
                setStatus(GameStatus.finished);
                if (getPlayerFinishState(opponent) == FinishState.FINISHED_SUCCESS) {
                    setWinner(null); // Tie
                } else {
                    setWinner(player);
                }
            } else {
                setStatus(GameStatus.waitingForFinalGuess);
                switchTurn();
            }
        } else {
            if (isFinalGuess) {
                setPlayerFinishState(player, FinishState.FINISHED_FAIL);
                setStatus(GameStatus.finished);
                setWinner(opponent);
            } else {
                switchTurn();
            }
        }
    }

    private void validateWordLength(WordChoice wordChoice) {
        if (wordChoice == null || wordChoice.word() == null) {
            return;
        }
        if (wordChoice.word().length() != config.wordLength().length()) {
            throw new IllegalArgumentException(
                    "Word '%s' must be %d letters long".formatted(wordChoice.word(), config.wordLength().length())
            );
        }
    }

    final String id;
    final GameConfig config;

    WordChoice playerOneWord;
    WordChoice playerTwoWord;
    GameStatus status;
    GamePlayer currentTurn;
    final List<GuessEntry> guesses;
    GamePlayer winner;
    private FinishState playerOneFinishState = FinishState.NOT_FINISHED;
    private FinishState playerTwoFinishState = FinishState.NOT_FINISHED;
}
