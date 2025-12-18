package controller;

import model.enums.FinishState;
import model.enums.GameStatus;
import model.GameState;
import model.Records.GamePlayer;
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
    public OutcomeViewModel buildMultiplayer(GameState state, Boolean winnerKnewWord) {
        if (state == null) return null;

        if (state.getStatus() == GameStatus.waitingForFinalGuess) {
            GamePlayer lastGuesser = state.getOpponent(state.getCurrentTurn());
            GamePlayer opponent = state.getCurrentTurn();
            String message = String.format(
                    "%s guessed the word! %s, you get one last chance to guess %s's word.",
                    name(lastGuesser),
                    name(opponent),
                    name(lastGuesser)
            );
            return new OutcomeViewModel("Last Chance!", message, null, null, NextAction.SHOW_LAST_CHANCE);
        }

        if (state.getStatus() != GameStatus.finished) {
            return null;
        }

        GamePlayer winner = state.getWinner();
        GamePlayer playerOne = state.getConfig().playerOne();
        GamePlayer playerTwo = state.getConfig().playerTwo();

        if (winner == null) {
            return new OutcomeViewModel("Game Result", "It's a Tie! Both players guessed the word.", "tie.png", SoundEffect.tie, NextAction.NONE);
        }

        // Winner exists
        if (winnerKnewWord == null) {
            String ask = String.format("%s, you guessed the word! Did you know this word?", name(winner));
            return new OutcomeViewModel("Win Condition", ask, null, null, NextAction.ASK_WINNER_KNOWLEDGE);
        }

        GamePlayer losingPlayer = winner.equals(playerOne) ? playerTwo : playerOne;
        boolean opponentAlsoSucceeded = state.getPlayerFinishState(losingPlayer) == FinishState.finishedSuccess;

        if (!winnerKnewWord) {
            String msg = String.format("Congratulations, %s! You won because you didn't know the word!", name(winner));
            return new OutcomeViewModel(winnerTitle(winner), msg, "win.png", SoundEffect.win, NextAction.NONE);
        }

        if (opponentAlsoSucceeded) {
            return new OutcomeViewModel("Game Result", "It's a Tie! Both of you knew your words.", "tie.png", SoundEffect.tie, NextAction.NONE);
        }

        String msg = String.format("Congratulations, %s! You won!", name(winner));
        return new OutcomeViewModel(winnerTitle(winner), msg, "win.png", SoundEffect.win, NextAction.NONE);
    }

    /**
     * Build a view model for solo game completion.
     * @param state the latest game state
     * @param playerKnewWord optional flag; null means we still need to ask
     */
    public OutcomeViewModel buildSolo(GameState state, Boolean playerKnewWord) {
        if (state == null || state.getStatus() != GameStatus.finished) {
            return null;
        }
        GamePlayer winner = state.getWinner();
        // In solo, winner == player means success
        if (winner != null) {
            if (playerKnewWord == null) {
                return new OutcomeViewModel("Win Condition", "You guessed the word! Did you know this word?", null, null, NextAction.ASK_WINNER_KNOWLEDGE);
            }
            if (!playerKnewWord) {
                return new OutcomeViewModel("You Win!", "Congratulations! You won because you didn't know the word!", "win.png", SoundEffect.win, NextAction.NONE);
            }
            return new OutcomeViewModel("You Win!", "Congratulations! You won!", "win.png", SoundEffect.win, NextAction.NONE);
        }

        // Player lost; show target word
        String targetWord = state.wordFor(state.getConfig().playerOne()).word();
        String msg = "Game Over! The word was: " + targetWord + ". You lost!";
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
}
