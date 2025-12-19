package model.rules;

import java.util.ArrayList;
import java.util.List;
import model.GuessResult;
import model.enums.LetterFeedback;

public class NormalEvaluator implements GuessEvaluator {

    @Override
    public GuessResult evaluate(String guess, String target) {
        Evaluation eval = evaluateCore(guess, target);
        return new GuessResult(guess, eval.feedback, eval.correctCount, eval.exactMatch);
    }

    protected Evaluation evaluateCore(String guess, String target) {
        int length = guess.length();
        List<LetterFeedback> feedback = new ArrayList<>(length);
        char[] guessChars = guess.toLowerCase().toCharArray();
        char[] targetChars = target.toLowerCase().toCharArray();
        boolean[] usedInTarget = new boolean[length];

        int correctCount = 0;
        boolean exactMatch = true;

        // First pass: correct positions
        for (int i = 0; i < length; i++) {
            if (guessChars[i] == targetChars[i]) {
                feedback.add(LetterFeedback.correct);
                usedInTarget[i] = true;
                correctCount++;
            } else {
                feedback.add(null);
                exactMatch = false;
            }
        }

        // Second pass: present or absent
        for (int i = 0; i < length; i++) {
            if (feedback.get(i) != null) continue;
            char c = guessChars[i];
            boolean found = false;
            for (int j = 0; j < length; j++) {
                if (!usedInTarget[j] && targetChars[j] == c) {
                    feedback.set(i, LetterFeedback.present);
                    usedInTarget[j] = true;
                    correctCount++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                feedback.set(i, LetterFeedback.notPresent);
            }
        }
        return new Evaluation(feedback, correctCount, exactMatch);
    }

    protected record Evaluation(List<LetterFeedback> feedback, int correctCount, boolean exactMatch) { }
}
