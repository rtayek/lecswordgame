package model.rules;

import java.util.ArrayList;
import model.GuessResult;
import model.enums.LetterFeedback;

/**
 * Expert mode returns no per-letter feedback, only counts and exact flag.
 */
public class ExpertEvaluator extends NormalEvaluator {
    @Override
    public GuessResult evaluate(String guess, String target) {
        Evaluation eval = evaluateCore(guess, target);
        return new GuessResult(guess, new ArrayList<LetterFeedback>(), eval.correctCount(), eval.exactMatch());
    }
}
