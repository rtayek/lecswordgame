package controller.events;

import java.util.List;
import controller.events.GameStatusView;
import controller.events.DifficultyView;

/**
 * Lightweight UI snapshot with only primitives/strings.
 */
public record GameUiModel(
        String gameId,
        GameStatusView status,
        DifficultyView difficulty,
        String currentPlayer,
        String winner,
        String provisionalWinner,
        String playerOne,
        String playerTwo,
        int timerDurationSeconds,
        Integer playerOneRemaining,
        Integer playerTwoRemaining,
        List<GuessView> guesses,
        KeyboardView keyboard
) { }
