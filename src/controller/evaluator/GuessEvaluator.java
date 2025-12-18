package controller.evaluator;

import model.Records.GuessResult;

public interface GuessEvaluator {
    GuessResult evaluate(String guess, String target);
}
