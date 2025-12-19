package controller.events;

import model.enums.GameStatus;

/**
 * Lightweight UI snapshot with only primitives/strings.
 */
public record GameUiModel(
        String gameId,
        GameStatus status,
        String currentPlayer,
        String winner,
        String provisionalWinner,
        String playerOne,
        String playerTwo,
        int timerDurationSeconds,
        Integer playerOneRemaining,
        Integer playerTwoRemaining
) { }
