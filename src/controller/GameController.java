package controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import model.Enums.*;
import model.GameState.GameConfig;
import model.GameState;
import model.Records.GamePlayer;
import model.Records.GuessEntry;
import model.Records.GuessResult;
import model.Records.WordChoice;

public class GameController {

    public GameState startNewGame(GameConfig config, WordChoice playerOneWord, WordChoice playerTwoWord) {
        var gameState = new GameState(config);
        gameState.setStatus(GameStatus.inProgress);
        gameState.setPlayerOneWord(playerOneWord);
        gameState.setPlayerTwoWord(playerTwoWord);
        return gameState;
    }

    public String pickWord(WordLength wordLength) {
        String[] words = dictionary.get(wordLength);
        if (words == null || words.length == 0) {
            throw new IllegalStateException("No words available for length " + wordLength.length());
        }
        int idx = random.nextInt(words.length);
        return words[idx];
    }

    public GameState submitGuess(GameState gameState, GamePlayer player, String rawGuess) {
        if (gameState == null) {
            throw new IllegalStateException("Start a new game first.");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null.");
        }
        if (gameState.getStatus() == GameStatus.finished) {
            throw new IllegalStateException("Game finished. Start a new word.");
        }

        // --- Input validation and processing ---
        var upper = rawGuess == null ? "" : rawGuess.trim().toUpperCase();
        var guess = upper.replaceAll("[^A-Z]", "");
        int expectedLength = gameState.getWordLength().length();

        if (guess.isEmpty()) {
            throw new IllegalArgumentException("Enter a guess first.");
        }
        if (guess.length() != expectedLength) {
            throw new IllegalArgumentException("Guess must be " + expectedLength + " letters.");
        }

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
        if (gameState.getMode() == GameMode.solo) {
            if (result.exactMatch()) {
                gameState.setWinner(player);
                gameState.setStatus(GameStatus.finished);
            }
            return gameState;
        }

        // --- Multiplayer Logic ---
        GamePlayer opponent = gameState.getOpponent(player);
        boolean isFinalGuess = gameState.getPlayerFinishState(opponent) != FinishState.NOT_FINISHED;

        if (result.exactMatch()) {
            gameState.setPlayerFinishState(player, FinishState.FINISHED_SUCCESS);
            if (isFinalGuess) {
                gameState.setStatus(GameStatus.finished);
                if (gameState.getPlayerFinishState(opponent) == FinishState.FINISHED_SUCCESS) {
                    gameState.setWinner(null); // Tie
                } else {
                    gameState.setWinner(player);
                }
            } else {
                gameState.setStatus(GameStatus.waitingForFinalGuess);
                gameState.switchTurn();
            }
        } else { // Incorrect guess
            if (isFinalGuess) {
                gameState.setPlayerFinishState(player, FinishState.FINISHED_FAIL);
                gameState.setStatus(GameStatus.finished);
                gameState.setWinner(opponent);
            } else {
                gameState.switchTurn();
            }
        }
        return gameState;
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
        return evaluateWithFeedback(
                guess,
                target,
                LetterFeedback.usedPresent,
                LetterFeedback.usedPresent,
                LetterFeedback.notInWord
        );
    }

    private GuessResult evaluateExpert(String guess, String target) {
        int correctLetterCount = evaluateOnlyCorrectCount(guess, target);
        boolean exactMatch = guess.equalsIgnoreCase(target);
        return new GuessResult(guess, List.of(), correctLetterCount, exactMatch);
    }

    private int evaluateOnlyCorrectCount(String guess, String target) {
        int length = guess.length();
        char[] guessChars = guess.toLowerCase().toCharArray();
        char[] targetChars = target.toLowerCase().toCharArray();
        boolean[] usedInTarget = new boolean[length];
        int correctLetterCount = 0;

        for (int i = 0; i < length; i++) {
            if (guessChars[i] == targetChars[i]) {
                usedInTarget[i] = true;
                correctLetterCount++;
            }
        }

        for (int i = 0; i < length; i++) {
            if (guessChars[i] == targetChars[i]) continue;
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
            if (feedback.get(i) != LetterFeedback.unused) continue;
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

    private static final Map<WordLength, String[]> dictionary = Map.of(
            WordLength.three, new String[]{"CAT", "SUN", "MAP"},
            WordLength.four, new String[]{"TREE", "LION", "BOAT"},
            WordLength.five, new String[]{"APPLE", "GRAPE", "PLANE", "BREAD"},
            WordLength.six, new String[]{"ORANGE", "PLANET", "STREAM"}
    );
    private final Random random = new Random();
}
