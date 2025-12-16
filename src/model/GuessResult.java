package model;

import java.util.ArrayList;
import java.util.List;
import model.Enums.LetterFeedback;

public record GuessResult(String guess, List<LetterFeedback> feedback, int correctLetterCount, boolean exactMatch) {

    public GuessResult {
        feedback = feedback == null ? List.of() : List.copyOf(feedback);
    }

    @Override
    public List<LetterFeedback> feedback() {
        return List.copyOf(feedback);
    }
}
