package controller;

import model.enums.Difficulty;
import model.enums.GameStatus;
import model.GameState;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.GuessEntry;
import model.Records.GuessResult;
import model.Records.WordChoice;

import model.Records.GuessOutcome;
import model.enums.WordLength;
import controller.evaluator.ExpertEvaluator;
import controller.evaluator.GuessEvaluator;
import controller.evaluator.HardEvaluator;
import controller.evaluator.NormalEvaluator;

public class GameController {

    private final DictionaryService dictionaryService;
    private final GuessEvaluator normalEvaluator = new NormalEvaluator();
    private final GuessEvaluator hardEvaluator = new HardEvaluator();
    private final GuessEvaluator expertEvaluator = new ExpertEvaluator();

    public GameController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    public GameState startNewGame(GameConfig config, WordChoice playerOneWord, WordChoice playerTwoWord) {
        var gameState = new GameState(config);

        WordChoice actualPlayerOneWord = playerOneWord;
        if (playerOneWord != null && playerOneWord.source() == model.enums.WordSource.rollTheDice) {
            actualPlayerOneWord = new WordChoice(dictionaryService.pickWord(config.wordLength()), model.enums.WordSource.rollTheDice);
        }

        WordChoice actualPlayerTwoWord = playerTwoWord;
        if (playerTwoWord != null && playerTwoWord.source() == model.enums.WordSource.rollTheDice) {
            actualPlayerTwoWord = new WordChoice(dictionaryService.pickWord(config.wordLength()), model.enums.WordSource.rollTheDice);
        }
        gameState.startWithChosenWords(config, actualPlayerOneWord, actualPlayerTwoWord);
        return gameState;
    }

    public String pickWord(WordLength wordLength) {
        return dictionaryService.pickWord(wordLength);
    }

    public boolean isValidWord(String word, WordLength wordLength) {
        return dictionaryService.isValidWord(word, wordLength);
    }

    public GuessOutcome submitGuess(GameState gameState, GamePlayer player, String rawGuess) {
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
        GuessResult result = evaluateGuess(guess, target, gameState.getConfig().difficulty());

        GuessEntry entry = new GuessEntry(player, result, System.currentTimeMillis());
        gameState.addGuess(entry);

        // --- State transition logic ---
        GameStatus oldStatus = gameState.getStatus();
        GamePlayer oldTurn = gameState.getCurrentTurn();

        gameState.applyGuessResult(player, result);

        GameStatus newStatus = gameState.getStatus();
        GamePlayer newTurn = gameState.getCurrentTurn();

        return new GuessOutcome(entry, newStatus, newTurn);
    }

    private GuessResult evaluateGuess(String guess, String target, Difficulty difficulty) {
        return switch (difficulty) {
            case normal -> normalEvaluator.evaluate(guess, target);
            case hard -> hardEvaluator.evaluate(guess, target);
            case expert -> expertEvaluator.evaluate(guess, target);
        };
    }
}
