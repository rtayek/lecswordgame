package controller.events;

import java.util.List;

/**
 * UI-friendly projection of a guess result.
 */
public record GuessResultView(
        String guess,
        List<LetterFeedbackView> feedback,
        int correctLetterCount,
        boolean exactMatch
) { }
