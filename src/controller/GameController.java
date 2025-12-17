package controller;

import java.util.ArrayList;
import java.util.List;
import model.Enums.Difficulty;
import model.Enums.LetterFeedback;
import model.Enums.GameStatus;
import model.GameState;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.GuessEntry;
import model.Records.GuessResult;
import model.Records.WordChoice;

public class GameController {

    private final DictionaryService dictionaryService;
    private final TimerController timerController;

    public GameController(DictionaryService dictionaryService, TimerController timerController) {
        this.dictionaryService = dictionaryService;
        this.timerController = timerController;
    }

    public GameState startNewGame(GameConfig config, WordChoice playerOneWord, WordChoice playerTwoWord) {
        var gameState = new GameState(config);
        gameState.setStatus(GameStatus.inProgress);

        WordChoice actualPlayerOneWord = playerOneWord;
        if (playerOneWord != null && playerOneWord.source() == model.Enums.WordSource.rollTheDice) {
            actualPlayerOneWord = new WordChoice(dictionaryService.pickWord(config.wordLength()), model.Enums.WordSource.rollTheDice);
        }
        gameState.setPlayerOneWord(actualPlayerOneWord);

        WordChoice actualPlayerTwoWord = playerTwoWord;
        if (playerTwoWord != null && playerTwoWord.source() == model.Enums.WordSource.rollTheDice) {
            actualPlayerTwoWord = new WordChoice(dictionaryService.pickWord(config.wordLength()), model.Enums.WordSource.rollTheDice);
        }
        gameState.setPlayerTwoWord(actualPlayerTwoWord);

        // Timer logic
        timerController.reset();
        int gameTime = config.timerDuration().seconds();
        if (config.timerDuration().isTimed()) {
            if (config.playerOne() != null) {
                timerController.setTimeForPlayer(config.playerOne(), gameTime);
            }
            if (config.playerTwo() != null) {
                timerController.setTimeForPlayer(config.playerTwo(), gameTime);
            }
            timerController.start(gameState.getCurrentTurn());
        }
        return gameState;
    }

    public String pickWord(model.Enums.WordLength wordLength) {
        return dictionaryService.pickWord(wordLength);
    }

    public boolean isValidWord(String word, model.Enums.WordLength wordLength) {
        return dictionaryService.isValidWord(word, wordLength);
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
        int expectedLength = gameState.getConfig().wordLength().length();

        if (guess.isEmpty()) {
            throw new IllegalArgumentException("Enter a guess first.");
        }
        if (guess.length() != expectedLength) {
            throw new IllegalArgumentException("Guess must be " + expectedLength + " letters.");
        }
        if (!dictionaryService.isValidWord(guess, gameState.getConfig().wordLength())) {
            throw new IllegalArgumentException("'" + guess + "' is not a valid word.");
        }

        WordChoice targetChoice = gameState.wordFor(player);
        if (targetChoice == null || targetChoice.word() == null) {
            throw new IllegalStateException("Target word is not set for this player.");
        }
        String target = targetChoice.word().trim();

        // --- Evaluate the guess ---
        GuessResult result;
        Difficulty difficulty = gameState.getConfig().difficulty();
        switch (difficulty) {
            case normal -> result = evaluateNormal(guess, target);
            case hard -> result = evaluateHard(guess, target);
            case expert -> result = evaluateExpert(guess, target);
            default -> throw new IllegalStateException("Unhandled difficulty: " + difficulty);
        }

        GuessEntry entry = new GuessEntry(player, result, System.currentTimeMillis());
        gameState.addGuess(entry);

        // --- State transition logic ---
        GameStatus oldStatus = gameState.getStatus();
        GamePlayer oldTurn = gameState.getCurrentTurn();

        gameState.applyGuessResult(player, result);

        GameStatus newStatus = gameState.getStatus();
        GamePlayer newTurn = gameState.getCurrentTurn();

        // Orchestrate timer based on state changes
        if (gameState.getConfig().timerDuration().isTimed()) {
            if (newStatus == GameStatus.finished && oldStatus != GameStatus.finished) {
                timerController.stop();
            } else if (newTurn != oldTurn) {
                timerController.start(newTurn);
            }
        }
        return gameState;
    }

    private GuessResult evaluateNormal(String guess, String target) {
        List<EvaluatedLetter> evaluatedLetters = evaluateCore(guess, target);
        List<LetterFeedback> feedback = new ArrayList<>();
        int correctLetterCount = 0;
        boolean exactMatch = true;

        for (EvaluatedLetter el : evaluatedLetters) {
            if (el.type() == MatchResultType.CORRECT_POSITION) {
                feedback.add(LetterFeedback.correctPosition);
                correctLetterCount++;
            } else if (el.type() == MatchResultType.WRONG_POSITION) {
                feedback.add(LetterFeedback.wrongPosition);
                correctLetterCount++; // Still "correct" in terms of being present
                exactMatch = false;
            } else {
                feedback.add(LetterFeedback.notInWord);
                exactMatch = false;
            }
        }
        return new GuessResult(guess, feedback, correctLetterCount, exactMatch);
    }

    private GuessResult evaluateHard(String guess, String target) {
        List<EvaluatedLetter> evaluatedLetters = evaluateCore(guess, target);
        List<LetterFeedback> feedback = new ArrayList<>();
        int correctLetterCount = 0;
        boolean exactMatch = true;

        for (EvaluatedLetter el : evaluatedLetters) {
            if (el.type() == MatchResultType.CORRECT_POSITION || el.type() == MatchResultType.WRONG_POSITION) {
                feedback.add(LetterFeedback.usedPresent); // Same feedback for correct/wrong position
                correctLetterCount++;
                if (el.type() == MatchResultType.WRONG_POSITION) {
                    exactMatch = false;
                }
            } else {
                feedback.add(LetterFeedback.notInWord);
                exactMatch = false;
            }
        }
        return new GuessResult(guess, feedback, correctLetterCount, exactMatch);
    }

    private GuessResult evaluateExpert(String guess, String target) {
        List<EvaluatedLetter> evaluatedLetters = evaluateCore(guess, target);
        int correctLetterCount = 0;
        boolean exactMatch = true;

        for (EvaluatedLetter el : evaluatedLetters) {
            if (el.type() == MatchResultType.CORRECT_POSITION || el.type() == MatchResultType.WRONG_POSITION) {
                correctLetterCount++;
                if (el.type() == MatchResultType.WRONG_POSITION) {
                    exactMatch = false;
                }
            } else {
                exactMatch = false;
            }
        }
        return new GuessResult(guess, List.of(), correctLetterCount, exactMatch);
    }

    private List<EvaluatedLetter> evaluateCore(String guess, String target) {
        int length = guess.length();
        List<EvaluatedLetter> evaluated = new ArrayList<>(length);
        char[] guessChars = guess.toLowerCase().toCharArray();
        char[] targetChars = target.toLowerCase().toCharArray();
        boolean[] usedInTarget = new boolean[length]; // Tracks which target chars have been matched

        // First pass: Find CORRECT_POSITION matches
        for (int i = 0; i < length; i++) {
            if (guessChars[i] == targetChars[i]) {
                evaluated.add(new EvaluatedLetter(guessChars[i], MatchResultType.CORRECT_POSITION));
                usedInTarget[i] = true;
            } else {
                evaluated.add(null); // Placeholder for now, will be filled in second pass
            }
        }

        // Second pass: Find WRONG_POSITION or NOT_IN_WORD matches
        for (int i = 0; i < length; i++) {
            if (evaluated.get(i) != null) { // Already matched in correct position
                continue;
            }

            char c = guessChars[i];
            boolean found = false;
            for (int j = 0; j < length; j++) {
                if (!usedInTarget[j] && targetChars[j] == c) {
                    evaluated.set(i, new EvaluatedLetter(c, MatchResultType.WRONG_POSITION));
                    usedInTarget[j] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                evaluated.set(i, new EvaluatedLetter(c, MatchResultType.NOT_IN_WORD));
            }
        }
        return evaluated;
    }

    private enum MatchResultType {
        CORRECT_POSITION,
        WRONG_POSITION,
        NOT_IN_WORD
    }

    private record EvaluatedLetter(char letter, MatchResultType type) {}
}
