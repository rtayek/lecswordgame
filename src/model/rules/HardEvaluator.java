package model.rules;

import model.GuessResult;

/**
 * Hard mode shares semantic feedback but the view colors correct/present the same.
 */
public class HardEvaluator extends NormalEvaluator {
    @Override
    public GuessResult evaluate(String guess, String target) {
        Evaluation eval = evaluateCore(guess, target);
        return new GuessResult(guess, eval.feedback(), eval.correctCount(), eval.exactMatch());
    }
}
