package controller.evaluator;

import model.Records.GuessResult;
import model.enums.LetterFeedback;
import java.util.ArrayList;

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
