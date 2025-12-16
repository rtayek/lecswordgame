package controller;

import java.util.ArrayList;
import java.util.List;

import model.Enums.*;
import model.GameState;
import model.Records.*;

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

    public GameState startNewSoloGame(GamePlayer player,
                                      Difficulty difficulty,
                                      WordLength wordLength,
                                      TimerDuration timerDuration,
                                      String targetWord) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }
        if (difficulty == null || wordLength == null || timerDuration == null) {
            throw new IllegalArgumentException("Game settings must not be null");
        }
        if (targetWord == null || targetWord.trim().isEmpty()) {
            throw new IllegalArgumentException("Target word must not be empty");
        }

        String trimmedTarget = targetWord.trim();
        if (trimmedTarget.length() != wordLength.length()) {
            throw new IllegalArgumentException("Target word must be " + wordLength.length() + " characters long");
        }

        gameState = new GameState(
                GameMode.solo,
                difficulty,
                wordLength,
                timerDuration,
                player,
                null
        );
        gameState.setStatus(GameStatus.inProgress);
        gameState.setPlayerOneWord(new WordChoice(trimmedTarget, model.Enums.WordSource.rollTheDice));
        gameState.setPlayerTwoWord(null);
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
        if (gameState.getStatus() == GameStatus.finished) {
            throw new IllegalStateException("Game is finished");
        }


        WordChoice targetChoice = gameState.wordFor(player);
        if (targetChoice == null || targetChoice.word() == null) {
            throw new IllegalStateException("Target word is not set for this player");
        }

        String target = targetChoice.word().trim();
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

        if (result.exactMatch()) {
        	gameState.setWinner(player);
            gameState.setStatus(GameStatus.finished);
        } else {
            gameState.switchTurn();
        }

        return result;
    }

    private GuessResult evaluateNormal(String guess, String target) {
        return evaluateWithFeedback(
                guess,
                target,
                LetterFeedback.correctPosition,
                LetterFeedback.wrongPosition,
                LetterFeedback.notInWord
        );
    }

    private GuessResult evaluateHard(String guess, String target) {
        // In hard mode, any used letter (correct spot or elsewhere) is marked the same.
        return evaluateWithFeedback(
                guess,
                target,
                LetterFeedback.usedPresent,
                LetterFeedback.usedPresent,
                LetterFeedback.notInWord
        );
    }

    private GuessResult evaluateExpert(String guess, String target) {
        // Expert returns the same feedback as normal; tweak here if expert should hide feedback.
        return evaluateNormal(guess, target);
    }

    private GuessResult evaluateWithFeedback(String guess,
                                             String target,
                                             LetterFeedback hitFeedback,
                                             LetterFeedback presentFeedback,
                                             LetterFeedback absentFeedback) {
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
                feedback.set(i, hitFeedback);
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
                feedback.set(i, presentFeedback);
                usedInTarget[foundIndex] = true;
                correctLetterCount++;
            } else {
                feedback.set(i, absentFeedback);
            }
        }

        boolean exactMatch = guess.equalsIgnoreCase(target);
        return new GuessResult(guess, feedback, correctLetterCount, exactMatch);
    }

    GameState gameState;
}
