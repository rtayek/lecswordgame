package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import model.GamePlayer;
import model.GuessEntry;
import model.WordChoice;
import model.enums.Difficulty;
import model.enums.FinishState;
import model.enums.GameMode;
import model.enums.GameStatus;
import model.enums.TimerDuration;
import model.enums.WordLength;
import model.GuessResult;

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

    /**
     * Initialize the game with chosen words and mark in progress.
     */
    public void startWithChosenWords(GameConfig cfg, WordChoice playerOneWord, WordChoice playerTwoWord) {
        if (!Objects.equals(cfg, this.config)) {
            throw new IllegalArgumentException("Config mismatch for game start");
        }
        setStatus(GameStatus.inProgress);
        this.winner = null;
        this.provisionalWinner = null;
        this.playerOneFinishState = FinishState.notFinished;
        this.playerTwoFinishState = FinishState.notFinished;
        setPlayerOneWord(playerOneWord);
        setPlayerTwoWord(playerTwoWord);
    }

    /**
     * Apply post-win adjudication based on whether the winner knew the word.
     */
    public void applyWinnerKnowledge(boolean winnerKnewWord) {
        if (status == GameStatus.awaitingWinnerKnowledge) {
            if (provisionalWinner == null) {
                return;
            }
            if (!winnerKnewWord) {
                finishWithWinner(provisionalWinner);
                return;
            }
            // Winner knew it: allow opponent a final guess
            setStatus(GameStatus.waitingForFinalGuess);
            switchTurn();
            return;
        }

        if (status != GameStatus.finished && status != GameStatus.soloChase) {
            return;
        }
        if (winner == null) {
            return;
        }
        if (config.mode() == GameMode.multiplayer && winnerKnewWord) {
            GamePlayer opponent = getOpponent(winner);
            if (getPlayerFinishState(opponent) == FinishState.finishedSuccess) {
                finishWithWinner(null); // tie
                return;
            }
        }
        // otherwise winner stands; mark finished
        finishWithWinner(winner);
    }

    /**
     * Handle a timer expiration for the given player: opponent wins by forfeit.
     */
    public void handleTimeout(GamePlayer expiredPlayer) {
        if (expiredPlayer == null) {
            return;
        }
        GamePlayer winner = getOpponent(expiredPlayer);
        // Mark finish states explicitly for clarity in UI/tests.
        setPlayerFinishState(expiredPlayer, FinishState.finishedFail);
        if (winner != null) {
            setPlayerFinishState(winner, FinishState.finishedSuccess);
        }
        finishWithWinner(winner);
    }

    void setStatus(GameStatus status) {
        this.status = status;
    }

    void setPlayerOneWord(WordChoice playerOneWord) {
        validateWordLength(playerOneWord);
        this.playerOneWord = playerOneWord;
    }

    void setPlayerTwoWord(WordChoice playerTwoWord) {
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

    public GamePlayer getProvisionalWinner() {
        return provisionalWinner;
    }

    void setWinner(GamePlayer winner) {
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

    void setPlayerFinishState(GamePlayer player, FinishState state) {
        if (player == null) return;
        if (Objects.equals(player, config.playerOne())) {
            this.playerOneFinishState = state;
        } else {
            this.playerTwoFinishState = state;
        }
    }

    public FinishState getPlayerFinishState(GamePlayer player) {
        if (player == null) return FinishState.notFinished;
        return Objects.equals(player, config.playerOne()) ? playerOneFinishState : playerTwoFinishState;
    }

    public void applyGuessResult(GamePlayer player, GuessResult result) {
        if (result == null) {
            return;
        }

        if (config.mode() == GameMode.solo) {
            if (result.exactMatch()) {
                finishWithWinner(player);
            }
            return;
        }

        GamePlayer opponent = getOpponent(player);
        boolean isFinalGuess = getPlayerFinishState(opponent) != FinishState.notFinished;

        if (result.exactMatch()) {
            markPlayerFinished(player, FinishState.finishedSuccess);
            if (isFinalGuess) {
                if (getPlayerFinishState(opponent) == FinishState.finishedSuccess) {
                    finishWithWinner(null); // Tie
                } else {
                    finishWithWinner(player);
                }
            } else {
                // First correct guess: capture provisional winner and wait for knowledge check
                this.provisionalWinner = player;
                setStatus(GameStatus.awaitingWinnerKnowledge);
                // do not switch turn until knowledge is resolved
            }
        } else {
            if (isFinalGuess) {
                markPlayerFinished(player, FinishState.finishedFail);
                setStatus(GameStatus.soloChase); // allow optional continue for fun
                finishWithWinner(opponent);
            } else {
                switchTurn();
            }
        }
    }

    private void markPlayerFinished(GamePlayer player, FinishState state) {
        setPlayerFinishState(player, state);
    }

    void finishWithWinner(GamePlayer winner) {
        setStatus(GameStatus.finished);
        setWinner(winner);
        provisionalWinner = null;
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
    GamePlayer provisionalWinner;
    private FinishState playerOneFinishState = FinishState.notFinished;
    private FinishState playerTwoFinishState = FinishState.notFinished;
}
