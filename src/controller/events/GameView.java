package controller.events;

import model.GamePlayer;
import model.GameState;
import model.enums.FinishState;
import model.enums.GameStatus;

/**
 * Lightweight projection of game state for listeners.
 */
public record GameView(
        String gameId,
        GameStatus status,
        GameState.GameConfig config,
        GamePlayer currentTurn,
        GamePlayer winner,
        GamePlayer provisionalWinner,
        FinishState playerOneState,
        FinishState playerTwoState
) {
    public FinishState finishStateFor(GamePlayer player) {
        if (player == null || config == null) return FinishState.notFinished;
        if (player.equals(config.playerOne())) {
            return playerOneState;
        }
        if (player.equals(config.playerTwo())) {
            return playerTwoState;
        }
        return FinishState.notFinished;
    }

    public GamePlayer opponentOf(GamePlayer player) {
        if (player == null || config == null) return null;
        return player.equals(config.playerOne()) ? config.playerTwo() : config.playerOne();
    }
}
