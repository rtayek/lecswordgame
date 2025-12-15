package model;

import java.util.ArrayList;
import java.util.List;
import model.Enums.LetterFeedback;

public class GuessResult {

    public GuessResult(String guess, List<LetterFeedback> feedback, int correctLetterCount, boolean exactMatch) {
        this.guess = guess;
        this.feedback = feedback == null ? List.of() : List.copyOf(feedback);
        this.correctLetterCount = correctLetterCount;
        this.exactMatch = exactMatch;
    }

    public String getGuess() {
        return guess;
    }

    public List<LetterFeedback> getFeedback() {
        return new ArrayList<>(feedback);
    }

    public int getCorrectLetterCount() {
        return correctLetterCount;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }

    final String guess;
    final List<LetterFeedback> feedback;
    final int correctLetterCount;
    final boolean exactMatch;
}
