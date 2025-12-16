package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
        if (player == null) 
            throw new IllegalArgumentException("Player must not be null");
 
        if (difficulty == null || wordLength == null || timerDuration == null) 
            throw new IllegalArgumentException("Game settings must not be null");

        String candidate = (targetWord == null || targetWord.trim().isEmpty())
                ? pickWord(wordLength)
                : targetWord.trim();

        if (candidate.length() != wordLength.length()) 
            throw new IllegalArgumentException("Target word must be " + wordLength.length() + " characters long");

        gameState = new GameState(
                GameMode.solo,
                difficulty,
                wordLength,
                timerDuration,
                player,
                null
        );
        gameState.setStatus(GameStatus.inProgress);
        gameState.setPlayerOneWord(null);
        gameState.setPlayerTwoWord(new WordChoice(candidate.toUpperCase(), model.Enums.WordSource.rollTheDice));
        return gameState;
    }

    public String pickWord(WordLength wordLength) {
        String[] words = dictionary.get(wordLength);
        if (words == null || words.length == 0) 
            throw new IllegalStateException("No words available for length " + wordLength.length());
        return words[random.nextInt(words.length)];
    }

    public GuessResult submitGuess(GamePlayer player, String rawGuess) {
        if (gameState == null) 
            throw new IllegalStateException("Start a new game first.");
        
        if (player == null) 
            throw new IllegalArgumentException("Player must not be null.");
        
        if (gameState.getStatus() == GameStatus.finished) {
            throw new IllegalStateException("Game finished. Start a new word.");
        }

        // --- Input validation and processing ---
        var upper = rawGuess == null ? "" : rawGuess.trim().toUpperCase();
        var guess = upper.replaceAll("[^A-Z]", "");
        int expectedLength = gameState.getWordLength().length();

        if (guess.isEmpty()) 
            throw new IllegalArgumentException("Enter a guess first.");
        
        if (guess.length() != expectedLength) 
            throw new IllegalArgumentException("Guess must be " + expectedLength + " letters.");
        
        WordChoice targetChoice = gameState.wordFor(player);
        if (targetChoice == null || targetChoice.word() == null) {
            throw new IllegalStateException("Target word is not set for this player.");
        }
        String target = targetChoice.word().trim();

        // --- Evaluate the guess ---
        GuessResult result;
        Difficulty difficulty = gameState.getDifficulty();
        switch (difficulty) {
            case normal -> result = evaluateNormal(guess, target);
            case hard -> result = evaluateHard(guess, target);
            case expert -> result = evaluateExpert(guess, target);
            default -> throw new IllegalStateException("Unhandled difficulty: " + difficulty);
        }

        GuessEntry entry = new GuessEntry(player, result, System.currentTimeMillis());
        gameState.addGuess(entry);

        // --- State transition logic ---
        // If solo mode, the logic is simple.
        if (gameState.getMode() == GameMode.solo) {
            if (result.exactMatch()) {
                gameState.setWinner(player);
                gameState.setStatus(GameStatus.finished);
            }
            // In solo mode, the turn doesn't switch.
            return result;
        }

        // --- Multiplayer Logic ---
        GamePlayer opponent = gameState.getOpponent(player);
        boolean isFinalGuess = gameState.getPlayerFinishState(opponent) != FinishState.NOT_FINISHED;

        if (result.exactMatch()) {
            gameState.setPlayerFinishState(player, FinishState.FINISHED_SUCCESS);
            if (isFinalGuess) {
                // Second player just finished successfully.
                gameState.setStatus(GameStatus.finished);
                // If opponent also succeeded, it's a tie. Otherwise, current player wins.
                if (gameState.getPlayerFinishState(opponent) == FinishState.FINISHED_SUCCESS) {
                    gameState.setWinner(null); // Tie
                } else {
                    gameState.setWinner(player);
                }
            } else {
                // First player just finished successfully. Wait for opponent.
                gameState.setStatus(GameStatus.waitingForFinalGuess);
                gameState.switchTurn();
            }
        } else { // Incorrect guess
            if (isFinalGuess) {
                // Second player just failed on their last chance.
                gameState.setPlayerFinishState(player, FinishState.FINISHED_FAIL);
                gameState.setStatus(GameStatus.finished);
                // Opponent is the winner.
                gameState.setWinner(opponent);
            } else {
                // Normal incorrect guess.
                gameState.switchTurn();
            }
        }

        return result;
    }

    GuessResult evaluateNormal(String guess, String target) {
        return evaluateWithFeedback(
                guess,
                target,
                LetterFeedback.correctPosition,
                LetterFeedback.wrongPosition,
                LetterFeedback.notInWord
        );
    }

    GuessResult evaluateHard(String guess, String target) {
        // In hard mode, any used letter (correct spot or elsewhere) is marked the same.
        return evaluateWithFeedback(
                guess,
                target,
                LetterFeedback.usedPresent,
                LetterFeedback.usedPresent,
                LetterFeedback.notInWord
        );
    }

    GuessResult evaluateExpert(String guess, String target) {
        int correctLetterCount = evaluateOnlyCorrectCount(guess, target);
        boolean exactMatch = guess.equalsIgnoreCase(target);
        return new GuessResult(guess, List.of(), correctLetterCount, exactMatch); // Empty feedback for expert mode
    }

    int evaluateOnlyCorrectCount(String guess, String target) {
        int length = guess.length();
        char[] guessChars = guess.toLowerCase().toCharArray();
        char[] targetChars = target.toLowerCase().toCharArray();
        boolean[] usedInTarget = new boolean[length];

        int correctLetterCount = 0;

        // First pass: check for correct position matches
        for (int i = 0; i < length; i++) {
            if (guessChars[i] == targetChars[i]) {
                usedInTarget[i] = true;
                correctLetterCount++;
            }
        }

        // Second pass: check for letters present in the word but wrong position
        for (int i = 0; i < length; i++) {
            // Only consider letters not already matched in the correct position
            if (guessChars[i] == targetChars[i]) { // This letter already handled in first pass
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
                usedInTarget[foundIndex] = true;
                correctLetterCount++;
            }
        }
        return correctLetterCount;
    }

    GuessResult evaluateWithFeedback(String guess,
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

    static final Map<WordLength, String[]> dictionary = Map.of(
            WordLength.three, new String[]{"CAT", "SUN", "MAP"},
            WordLength.four, new String[]{"TREE", "LION", "BOAT"},
            WordLength.five, new String[]{"APPLE", "GRAPE", "PLANE", "BREAD"},
            WordLength.six, new String[]{"ORANGE", "PLANET", "STREAM"}
    );
    final Random random = new Random();
}
