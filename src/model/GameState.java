package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import model.Enums.*;
import model.Records.GamePlayer;
import model.Records.GuessEntry;
import model.Records.WordChoice;

public class GameState {

    public GameState(GameMode mode,
                     Difficulty difficulty,
                     WordLength wordLength,
                     TimerDuration timerDuration,
                     GamePlayer playerOne,
                     GamePlayer playerTwo) {
        this(UUID.randomUUID().toString(), mode, difficulty, wordLength, timerDuration, playerOne, playerTwo,
                GameStatus.setup, playerOne);
    }

    public GameState(String id,
                     GameMode mode,
                     Difficulty difficulty,
                     WordLength wordLength,
                     TimerDuration timerDuration,
                     GamePlayer playerOne,
                     GamePlayer playerTwo,
                     GameStatus status,
                     GamePlayer startingTurn) {
        this.id = Objects.requireNonNullElseGet(id, () -> UUID.randomUUID().toString());
        this.mode = mode;
        this.difficulty = difficulty;
        this.wordLength = wordLength;
        this.timerDuration = timerDuration;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.status = status;
        this.currentTurn = startingTurn;
        this.guesses = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public GameMode getMode() {
        return mode;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public WordLength getWordLength() {
        return wordLength;
    }

    public TimerDuration getTimerDuration() {
        return timerDuration;
    }

    public GamePlayer getPlayerOne() {
        return playerOne;
    }

    public GamePlayer getPlayerTwo() {
        return playerTwo;
    }

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
        this.playerOneWord = playerOneWord;
    }

    public void setPlayerTwoWord(WordChoice playerTwoWord) {
        this.playerTwoWord = playerTwoWord;
    }

    public void switchTurn() {
        if (playerOne != null && playerTwo != null) {
            currentTurn = Objects.equals(currentTurn, playerOne) ? playerTwo : playerOne;
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
        var isPlayerOne = Objects.equals(player, playerOne);
        return isPlayerOne ? playerOneWord : playerTwoWord;
    }

    final String id;
    final GameMode mode;
    final Difficulty difficulty;
    final WordLength wordLength;
    final TimerDuration timerDuration;
    final GamePlayer playerOne;
    final GamePlayer playerTwo;
    WordChoice playerOneWord;
    WordChoice playerTwoWord;
    GameStatus status;
    GamePlayer currentTurn;
    final List<GuessEntry> guesses;
    GamePlayer winner;
}
