package controller.events;

import model.GuessResult;

/**
 * Immutable view of a single guess.
 */
public record GuessView(
        String playerName,
        boolean isPlayerOne,
        GuessResult result
) { }
