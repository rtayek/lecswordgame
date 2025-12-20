package controller;

import model.GamePlayer;
import model.enums.FinishState;
import model.enums.GameStatus;
import util.SoundEffect;

/**
 * Produces view models for game outcomes and last-chance prompts.
 */
public class GameOutcomePresenter {

    public enum NextAction {
        NONE,
        ASK_WINNER_KNOWLEDGE,
        SHOW_LAST_CHANCE
    }

    public record OutcomeViewModel(
            String title,
            String message,
            String graphicFile,
            SoundEffect soundEffect,
            NextAction nextAction
    ) { }

    /**
     * Build a view model for multiplayer end-of-turn/end-of-game situations.
     * @param state the latest game state
     * @param winnerKnewWord optional flag; null means we still need to ask the winner
     */
    public OutcomeViewModel buildMultiplayer(controller.events.GameUiModel state, Boolean winnerKnewWord) {
        if (state == null) return null;

        if ("waitingForFinalGuess".equalsIgnoreCase(state.status())) {
            String lastGuesser = state.provisionalWinner() != null ? state.provisionalWinner() : state.currentPlayer();
            String opponent = state.currentPlayer();
            String message = String.format(
                    "%s guessed the word! %s, you get one last chance to guess %s's word.",
                    safeName(lastGuesser),
                    safeName(opponent),
                    safeName(lastGuesser)
            );
            return new OutcomeViewModel("Last Chance!", message, null, null, NextAction.SHOW_LAST_CHANCE);
        }

        if ("awaitingWinnerKnowledge".equalsIgnoreCase(state.status())) {
            String winner = state.provisionalWinner() != null ? state.provisionalWinner() : state.currentPlayer();
            String ask = String.format("%s, you guessed the word! Did you know this word?", safeName(winner));
            return new OutcomeViewModel("Win Condition", ask, null, null, NextAction.ASK_WINNER_KNOWLEDGE);
        }

        if (!"finished".equalsIgnoreCase(state.status())) {
            return null;
        }

        String winner = state.winner();

        if (winner == null) {
            return new OutcomeViewModel("Game Result", "It's a Tie! Both players guessed the word.", "tie.png", SoundEffect.tie, NextAction.NONE);
        }

        // Winner exists
        if (winnerKnewWord == null) {
            String ask = String.format("%s, you guessed the word! Did you know this word?", safeName(winner));
            return new OutcomeViewModel("Win Condition", ask, null, null, NextAction.ASK_WINNER_KNOWLEDGE);
        }

        if (!winnerKnewWord) {
            String msg = String.format("Congratulations, %s! You won because you didn't know the word!", safeName(winner));
            return new OutcomeViewModel(winner + " Wins!", msg, "win.png", SoundEffect.win, NextAction.NONE);
        }

        String msg = String.format("Congratulations, %s! You won!", safeName(winner));
        return new OutcomeViewModel(winner + " Wins!", msg, "win.png", SoundEffect.win, NextAction.NONE);
    }

    /**
     * Build a view model for solo game completion.
     * @param state the latest game state
     * @param playerKnewWord optional flag; null means we still need to ask
     */
    public OutcomeViewModel buildSolo(controller.events.GameUiModel state, Boolean playerKnewWord) {
        if (state == null || !"finished".equalsIgnoreCase(state.status())) {
            return null;
        }
        String winner = state.winner();
        if (winner != null) {
            if (playerKnewWord == null) {
                return new OutcomeViewModel("Win Condition", "You guessed the word! Did you know this word?", null, null, NextAction.ASK_WINNER_KNOWLEDGE);
            }
            if (!playerKnewWord) {
                return new OutcomeViewModel("You Win!", "Congratulations! You won because you didn't know the word!", "win.png", SoundEffect.win, NextAction.NONE);
            }
            return new OutcomeViewModel("You Win!", "Congratulations! You won!", "win.png", SoundEffect.win, NextAction.NONE);
        }

        // Player lost
        String msg = "Game Over! You lost!";
        return new OutcomeViewModel("You Lose!", msg, "lose.png", SoundEffect.lose, NextAction.NONE);
    }

    private String name(GamePlayer player) {
        if (player == null || player.profile() == null || player.profile().username() == null || player.profile().username().isBlank()) {
            return "Player";
        }
        return player.profile().username();
    }

    private String winnerTitle(GamePlayer winner) {
        return name(winner) + " Wins!";
    }
    
    private String safeName(String name) {
        return (name == null || name.isBlank()) ? "Player" : name;
    }
}
