package model;

import model.Enums.Difficulty;
import model.Enums.WordLength;

public class GameLogEntry {

    public GameLogEntry(String gameId,
                        String playerOneName,
                        String playerTwoName,
                        Difficulty difficulty,
                        WordLength wordLength,
                        String resultSummary) {
        this.gameId = gameId;
        this.playerOneName = playerOneName;
        this.playerTwoName = playerTwoName;
        this.difficulty = difficulty;
        this.wordLength = wordLength;
        this.resultSummary = resultSummary;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerOneName() {
        return playerOneName;
    }

    public String getPlayerTwoName() {
        return playerTwoName;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public WordLength getWordLength() {
        return wordLength;
    }

    public String getResultSummary() {
        return resultSummary;
    }

    final String gameId;
    final String playerOneName;
    final String playerTwoName;
    final Difficulty difficulty;
    final WordLength wordLength;
    final String resultSummary;
}
