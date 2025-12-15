package controller;

import java.util.ArrayList;
import java.util.List;

import model.Enums.Difficulty;
import model.Enums.GameMode;
import model.Enums.GameStatus;
import model.Enums.TimerDuration;
import model.Enums.WordLength;
import model.Enums.LetterFeedback;
import model.GamePlayer;
import model.GameState;
import model.GuessEntry;
import model.GuessResult;
import model.WordChoice;

public class GameController {

    public GameState getGameState() {
        return gameState;
    }

    public GameState startNewMultiplayerGame(GamePlayer playerOne,
                                             GamePlayer playerTwo,
                                             Difficulty difficulty,
                                             WordLength wordLength,
                                             TimerDuration timerDuration) {
        gameState = new GameState(
                GameMode.multiplayer,
                difficulty,
                wordLength,
                timerDuration,
                playerOne,
                playerTwo
        );
        gameState.setStatus(GameStatus.inProgress);
        return gameState;
    }

    public GuessResult submitGuess(GamePlayer player, String guess) {
        if (gameState == null) {
            throw new IllegalStateException("No active game");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        if (guess == null) {
            throw new IllegalArgumentException("Guess must not be null");
        }

        WordChoice targetChoice = gameState.wordFor(player);
        if (targetChoice == null || targetChoice.getWord() == null) {
            throw new IllegalStateException("Target word is not set for this player");
        }

        String target = targetChoice.getWord().trim();
        String trimmedGuess = guess.trim();
        if (target.isEmpty() || trimmedGuess.isEmpty()) {
            throw new IllegalArgumentException("Words must not be empty");
        }

        int expectedLength = gameState.getWordLength().length();
        if (trimmedGuess.length() != expectedLength) {
            throw new IllegalArgumentException("Guess must be " + expectedLength + " characters long");
        }
        if (target.length() != expectedLength) {
            throw new IllegalStateException("Target word length does not match game setting");
        }

        GuessResult result;
        Difficulty difficulty = gameState.getDifficulty();
        switch (difficulty) {
            case normal -> result = evaluateNormal(trimmedGuess, target);
            case hard -> result = evaluateHard(trimmedGuess, target);
            case expert -> result = evaluateExpert(trimmedGuess, target);
            default -> throw new IllegalStateException("Unhandled difficulty: " + difficulty);
        }

        GuessEntry entry = new GuessEntry(player, result, System.currentTimeMillis());
        gameState.addGuess(entry);

        if (result.isExactMatch()) {
            gameState.setStatus(GameStatus.finished);
        } else {
            gameState.switchTurn();
        }

        return result;
    }

    private GuessResult evaluateNormal(String guess, String target) {
        int length = guess.length();
        List<LetterFeedback> feedback = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            feedback.add(LetterFeedback.unused);
        }

        char[] guessChars = guess.toLowerCase().toCharArray();
        char[] targetChars = target.toLowerCase().toCharArray();
        boolean[] usedInTarget = new boolean[length];

        int correctLetterCount = 0;

        for (int i = 0; i < length; i++) {
            if (guessChars[i] == targetChars[i]) {
                feedback.set(i, LetterFeedback.correctPosition);
                usedInTarget[i] = true;
                correctLetterCount++;
            }
        }

        for (int i = 0; i < length; i++) {
            if (feedback.get(i) != LetterFeedback.unused) {
                continue;
            }
            char c = guessChars[i];
            int foundIndex = -1;
            for (int j = 0; j < length; j++) {
                if (!usedInTarget[j] && targetChars[j] == c) {
                    foundIndex = j;
                    break;
                }
            }
            if (foundIndex >= 0) {
                feedback.set(i, LetterFeedback.wrongPosition);
                usedInTarget[foundIndex] = true;
                correctLetterCount++;
            } else {
                feedback.set(i, LetterFeedback.notInWord);
            }
        }

        boolean exactMatch = guess.equalsIgnoreCase(target);
        return new GuessResult(guess, feedback, correctLetterCount, exactMatch);
    }

    private GuessResult evaluateHard(String guess, String target) {
        int length = guess.length();
        List<LetterFeedback> feedback = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            feedback.add(LetterFeedback.unused);
        }

        char[] guessChars = guess.toLowerCase().toCharArray();
        char[] targetChars = target.toLowerCase().toCharArray();
        boolean[] usedInTarget = new boolean[length];

        int correctLetterCount = 0;

        for (int i = 0; i < length; i++) {
            if (guessChars[i] == targetChars[i]) {
                feedback.set(i, LetterFeedback.usedPresent);
                usedInTarget[i] = true;
                correctLetterCount++;
            }
        }

        for (int i = 0; i < length; i++) {
            if (feedback.get(i) != LetterFeedback.unused) {
                continue;
            }
            char c = guessChars[i];
            int foundIndex = -1;
            for (int j = 0; j < length; j++) {
                if (!usedInTarget[j] && targetChars[j] == c) {
                    foundIndex = j;
                    break;
                }
            }
            if (foundIndex >= 0) {
                feedback.set(i, LetterFeedback.usedPresent);
                usedInTarget[foundIndex] = true;
                correctLetterCount++;
            } else {
                feedback.set(i, LetterFeedback.notInWord);
            }
        }

        boolean exactMatch = guess.equalsIgnoreCase(target);
        return new GuessResult(guess, feedback, correctLetterCount, exactMatch);
    }

    private GuessResult evaluateExpert(String guess, String target) {
        GuessResult normal = evaluateNormal(guess, target);
        return new GuessResult(
                guess,
                List.of(),
                normal.getCorrectLetterCount(),
                normal.isExactMatch()
        );
    }

    GameState gameState;
}
