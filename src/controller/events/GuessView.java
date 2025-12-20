package controller.events;

/**
 * Immutable view of a single guess.
 */
public record GuessView(
        String playerName,
        boolean isPlayerOne,
        GuessResultView result
) { }
