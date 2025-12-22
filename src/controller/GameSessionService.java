package controller;

import java.util.ArrayList;
import java.util.List;
import model.GameState;
import model.GameState.GameConfig;
import model.GamePlayer;
import model.GuessOutcome;
import model.WordChoice;
import model.enums.GameMode;
import model.enums.GameStatus;
import model.enums.WordLength;
import controller.events.PlayerSlot;
import controller.TurnTimer;
import controller.events.GameEvent;
import controller.events.GameEvent.GameEventKind;
import controller.events.GameEventListener;
import controller.GameUiModelMapper;
import controller.KeyboardViewBuilder;

/**
 * Orchestrates the lifecycle of a single game session and fans out state/events.
 */
public class GameSessionService implements TurnTimer.Listener {

    private final GameController gameController;
    private final TurnTimer turnTimer;
    private final GameUiModelMapper uiMapper;

    private GameState currentGameState;
    private final List<GameEventListener> eventListeners = new ArrayList<>();

    public GameSessionService(GameController gameController, TurnTimer turnTimer) {
        this.gameController = gameController;
        this.turnTimer = turnTimer;
        this.turnTimer.addListener(this);
        this.uiMapper = new GameUiModelMapper(turnTimer, new KeyboardViewBuilder());
    }

    public void addEventListener(GameEventListener listener) {
        if (listener != null) {
            eventListeners.add(listener);
        }
    }

    GameState getCurrentGameState() {
        return currentGameState;
    }

    public GameState startNewGame(GameConfig config, WordChoice playerOneWord, WordChoice playerTwoWord) {
        currentGameState = gameController.startNewGame(config, playerOneWord, playerTwoWord);
        if (config.timerDuration().isTimed()) {
            turnTimer.reset();
            int gameTime = config.timerDuration().seconds();
            if (config.playerOne() != null) {
                turnTimer.setTimeForPlayer(PlayerSlot.playerOne, gameTime);
            }
            if (config.playerTwo() != null) {
                turnTimer.setTimeForPlayer(PlayerSlot.playerTwo, gameTime);
            }
            var slot = slotFor(currentGameState.getCurrentTurn());
            if (slot != null) {
                turnTimer.start(slot);
            }
        } else {
            turnTimer.reset();
        }
        publish(GameEventKind.gameStarted);
        return currentGameState;
    }

    public void submitGuess(String guess) {
        if (currentGameState == null) {
            throw new IllegalStateException("Start a new game first.");
        }
        if (currentGameState.getStatus() == GameStatus.awaitingWinnerKnowledge) {
            throw new IllegalStateException("Awaiting winner knowledge; no guesses allowed.");
        }
        GamePlayer player = currentGameState.getCurrentTurn();
        GuessOutcome outcome = gameController.submitGuess(currentGameState, player, guess);
        GameStatus newStatus = outcome.status();
        GamePlayer nextTurn = outcome.nextTurn();

        publish(GameEventKind.gameStateUpdated);

        if (newStatus == GameStatus.finished) {
            publish(GameEventKind.gameFinished);
            turnTimer.stop();
        } else if (newStatus == GameStatus.awaitingWinnerKnowledge) {
            // Pause timers while waiting for winner knowledge response.
            turnTimer.stop();
            publish(GameEventKind.gameFinished);
        } else if (currentGameState.getConfig().timerDuration().isTimed() && nextTurn != null && nextTurn != player) {
            var slot = slotFor(nextTurn);
            if (slot != null) {
                turnTimer.start(slot);
            }
        }
    }

    public void reset() {
        currentGameState = null;
        turnTimer.reset();
    }

    public String pickWord(WordLength length) {
        return gameController.pickWord(length);
    }

    public boolean isValidWord(String word, WordLength length) {
        return gameController.isValidWord(word, length);
    }

    public void applyWinnerKnowledge(boolean winnerKnewWord) {
        if (currentGameState == null) return;
        GameStatus before = currentGameState.getStatus();
        currentGameState.applyWinnerKnowledge(winnerKnewWord);
        GameStatus after = currentGameState.getStatus();

        if (after == GameStatus.finished || after == GameStatus.soloChase) {
            publish(GameEventKind.gameFinished);
            turnTimer.stop();
        } else {
            publish(GameEventKind.gameStateUpdated);
            publish(GameEventKind.gameFinished);
            if (after == GameStatus.waitingForFinalGuess
                    && currentGameState.getConfig().timerDuration().isTimed()
                    && currentGameState.getCurrentTurn() != null) {
                var slot = slotFor(currentGameState.getCurrentTurn());
                if (slot != null) {
                    turnTimer.start(slot);
                }
            }
        }
    }

    @Override
    public void onTimeUpdated(PlayerSlot slot, int remainingSeconds) {
        if (currentGameState == null || slot == null) return;
        publish(GameEventKind.timerUpdated);
    }

    @Override
    public void onTimeExpired(PlayerSlot slot) {
        if (currentGameState == null || slot == null) {
            return;
        }
        if (!currentGameState.getConfig().timerDuration().isTimed()) {
            return;
        }
        if (currentGameState.getStatus() == GameStatus.finished) {
            return;
        }
        GamePlayer player = playerForSlot(slot);
        currentGameState.handleTimeout(player);
        turnTimer.stop();
        publish(GameEventKind.timerExpired);
        publish(GameEventKind.gameFinished);
    }

    private void publish(GameEventKind kind) {
        var event = new GameEvent(kind, uiMapper.toUiModel(currentGameState));
        for (GameEventListener l : eventListeners) {
            l.onGameEvent(event);
        }
    }

    private PlayerSlot slotFor(GamePlayer player) {
        if (player == null || currentGameState == null || currentGameState.getConfig() == null) return null;
        if (player.equals(currentGameState.getConfig().playerOne())) {
            return PlayerSlot.playerOne;
        }
        if (player.equals(currentGameState.getConfig().playerTwo())) {
            return PlayerSlot.playerTwo;
        }
        return null;
    }

    private GamePlayer playerForSlot(PlayerSlot slot) {
        if (slot == null || currentGameState == null || currentGameState.getConfig() == null) return null;
        return switch (slot) {
            case playerOne -> currentGameState.getConfig().playerOne();
            case playerTwo -> currentGameState.getConfig().playerTwo();
        };
    }
}
