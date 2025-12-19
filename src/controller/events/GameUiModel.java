package controller.events;

import java.util.List;
import model.enums.Difficulty;
import model.enums.GameStatus;

/**
 * Lightweight UI snapshot with only primitives/strings.
 */
public record GameUiModel(
        String gameId,
        GameStatus status,
        Difficulty difficulty,
        String currentPlayer,
        String winner,
        String provisionalWinner,
        String playerOne,
        String playerTwo,
        int timerDurationSeconds,
        Integer playerOneRemaining,
        Integer playerTwoRemaining,
        List<GuessView> guesses,
        KeyboardView keyboard,
        String targetWord
) { }
