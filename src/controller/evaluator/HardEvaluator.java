package controller.evaluator;

import model.Records.GuessResult;
import model.enums.LetterFeedback;

/**
 * Hard mode shares semantic feedback but view will map positions uniformly.
 */
public class HardEvaluator extends NormalEvaluator {
    @Override
    public GuessResult evaluate(String guess, String target) {
        Evaluation eval = evaluateCore(guess, target);
        // Feedback retained; view will color correct/present the same.
        return new GuessResult(guess, eval.feedback(), eval.correctCount(), eval.exactMatch());
    }
}
