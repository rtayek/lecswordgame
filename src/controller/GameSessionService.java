package controller;

import java.util.ArrayList;
import java.util.List;
import model.Enums.GameMode;
import model.Enums.GameStatus;
import model.GameState;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.GuessOutcome;
import model.Records.WordChoice;
import view.listeners.GameEventListener;
import view.listeners.GameStateListener;

/**
 * Orchestrates the lifecycle of a single game session and fans out state/events.
 */
public class GameSessionService {

    private final GameController gameController;
    private final TurnTimer turnTimer;

    private GameState currentGameState;
    private final List<GameStateListener> stateListeners = new ArrayList<>();
    private final List<GameEventListener> eventListeners = new ArrayList<>();

    public GameSessionService(GameController gameController, TurnTimer turnTimer) {
        this.gameController = gameController;
        this.turnTimer = turnTimer;
    }

    public void addStateListener(GameStateListener listener) {
        if (listener != null) {
            stateListeners.add(listener);
        }
    }

    public void addEventListener(GameEventListener listener) {
        if (listener != null) {
            eventListeners.add(listener);
        }
    }

    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public GameState startNewGame(GameConfig config, WordChoice playerOneWord, WordChoice playerTwoWord) {
        currentGameState = gameController.startNewGame(config, playerOneWord, playerTwoWord);
        if (config.timerDuration().isTimed()) {
            turnTimer.reset();
            int gameTime = config.timerDuration().seconds();
            if (config.playerOne() != null) {
                turnTimer.setTimeForPlayer(config.playerOne(), gameTime);
            }
            if (config.playerTwo() != null) {
                turnTimer.setTimeForPlayer(config.playerTwo(), gameTime);
            }
            turnTimer.start(currentGameState.getCurrentTurn());
        } else {
            turnTimer.reset();
        }
        stateListeners.forEach(l -> l.onGameStateUpdate(currentGameState));
        eventListeners.forEach(l -> l.onGameStart(currentGameState));
        return currentGameState;
    }

    public GuessOutcome submitGuess(String guess) {
        if (currentGameState == null) {
            throw new IllegalStateException("Start a new game first.");
        }
        GamePlayer player = currentGameState.getCurrentTurn();
        GuessOutcome outcome = gameController.submitGuess(currentGameState, player, guess);
        GameStatus newStatus = outcome.status();
        GamePlayer nextTurn = outcome.nextTurn();

        // GameState is mutated in submitGuess; notify listeners with updated state.
        stateListeners.forEach(l -> l.onGameStateUpdate(currentGameState));

        if (newStatus == GameStatus.finished) {
            eventListeners.forEach(l -> l.onGameOver(currentGameState));
        }

        if (currentGameState.getConfig().timerDuration().isTimed()) {
            if (newStatus == GameStatus.finished) {
                turnTimer.stop();
            } else if (nextTurn != null && nextTurn != player) {
                turnTimer.start(nextTurn);
            }
        }
        return outcome;
    }

    public void reset() {
        currentGameState = null;
        turnTimer.reset();
    }
}
