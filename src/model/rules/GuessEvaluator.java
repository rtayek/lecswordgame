package model.rules;

import model.GuessResult;

/**
 * Pure evaluation contract: interpret a guess against a target word.
 */
public interface GuessEvaluator {
    GuessResult evaluate(String guess, String target);
}
