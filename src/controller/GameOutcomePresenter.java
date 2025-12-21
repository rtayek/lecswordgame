package controller;

import controller.events.GameStatusView;
import util.SoundEffect;

/**
 * Produces view models for game outcomes and last-chance prompts.
 */
public class GameOutcomePresenter {

    public OutcomeViewModel build(controller.events.GameUiModel state, Boolean winnerKnewWord) {
        if (state == null) return null;

        return switch (state.status()) {
            case waitingForFinalGuess -> {
                String lastGuesser = state.provisionalWinner() != null ? state.provisionalWinner() : state.currentPlayer();
                String opponent = state.currentPlayer();
                String message = String.format(
                        "%s guessed the word! %s, you get one last chance to guess %s's word.",
                        safeName(lastGuesser),
                        safeName(opponent),
                        safeName(lastGuesser)
                );
                yield new OutcomeViewModel("Last Chance!", message, null, null, NextAction.SHOW_LAST_CHANCE);
            }
            case awaitingWinnerKnowledge -> {
                String winner = state.provisionalWinner() != null ? state.provisionalWinner() : state.currentPlayer();
                String ask = String.format("%s, you guessed the word! Did you know this word?", safeName(winner));
                yield new OutcomeViewModel("Win Condition", ask, null, null, NextAction.ASK_WINNER_KNOWLEDGE);
            }
            case finished -> {
                String winner = state.winner();
                if (winner == null) {
                    yield new OutcomeViewModel("Game Result", "It's a Tie! Both players guessed the word.", "tie.png", SoundEffect.tie, NextAction.NONE);
                }

                if (winnerKnewWord == null) {
                    String ask = String.format("%s, you guessed the word! Did you know this word?", safeName(winner));
                    yield new OutcomeViewModel("Win Condition", ask, null, null, NextAction.ASK_WINNER_KNOWLEDGE);
                }

                if (!winnerKnewWord) {
                    String msg = String.format("Congratulations, %s! You won because you didn't know the word!", safeName(winner));
                    yield new OutcomeViewModel(winner + " Wins!", msg, "win.png", SoundEffect.win, NextAction.NONE);
                }

                String msg = String.format("Congratulations, %s! You won!", safeName(winner));
                yield new OutcomeViewModel(winner + " Wins!", msg, "win.png", SoundEffect.win, NextAction.NONE);
            }
            default -> null;
        };
    }
    
    private String safeName(String name) {
        return (name == null || name.isBlank()) ? "Player" : name;
    }
}
