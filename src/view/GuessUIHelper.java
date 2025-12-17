package view;

import controller.GameController;
import java.util.function.Function;
import model.Enums.GameStatus;
import model.GameState;
import model.Records.GamePlayer;
import model.Records.GuessResult;

/**
 * Shared helper for submitting guesses from UI panels to keep validation in one place.
 */
final class GuessUIHelper {

    static Outcome submitGuess(Navigation navigation,
                               GameController controller,
                               Function<GameState, GamePlayer> currentPlayerProvider,
                               String rawGuess,
                               String missingStateMessage) {

        GameState state = navigation.getGameState();
        if (state == null) {
            return Outcome.error(missingStateMessage);
        }

        if (state.getStatus() == GameStatus.finished) {
            return Outcome.error("Game finished.");
        }

        GamePlayer player = currentPlayerProvider.apply(state);
        if (player == null) {
            return Outcome.error("No current player.");
        }

        var wordLength = state.getConfig().wordLength();
        String guess = normalizeGuess(rawGuess);
        if (guess.isEmpty()) {
            return Outcome.error("Enter a guess first.");
        }
        if (wordLength != null && guess.length() != wordLength.length()) {
            return Outcome.error("Guess must be " + wordLength.length() + " letters.");
        }

        try {
            GameState updatedState = controller.submitGuess(state, player, guess);
            var guesses = updatedState.getGuesses();
            if (guesses.isEmpty()) {
                return Outcome.error("No guesses recorded.");
            }
            GuessResult result = guesses.get(guesses.size() - 1).result();
            navigation.setGameState(updatedState);
            return Outcome.success(updatedState, result, player);
        } catch (RuntimeException ex) {
            return Outcome.error(ex.getMessage());
        }
    }

    private static String normalizeGuess(String raw) {
        var upper = raw == null ? "" : raw.trim().toUpperCase();
        return upper.replaceAll("[^A-Z]", "");
    }

    static final class Outcome {
        private final GameState state;
        private final GuessResult result;
        private final GamePlayer player;
        private final String errorMessage;

        private Outcome(GameState state, GuessResult result, GamePlayer player, String errorMessage) {
            this.state = state;
            this.result = result;
            this.player = player;
            this.errorMessage = errorMessage;
        }

        static Outcome success(GameState state, GuessResult result, GamePlayer player) {
            return new Outcome(state, result, player, null);
        }

        static Outcome error(String message) {
            return new Outcome(null, null, null, message);
        }

        boolean isError() {
            return errorMessage != null && !errorMessage.isBlank();
        }

        String errorMessage() {
            return errorMessage;
        }

        GameState state() {
            return state;
        }

        GuessResult result() {
            return result;
        }

        GamePlayer player() {
            return player;
        }
    }

    private GuessUIHelper() { }
}
