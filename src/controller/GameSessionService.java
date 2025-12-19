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
import controller.TurnTimer;
import controller.events.GameEvent;
import controller.events.GameEvent.GameEventKind;
import controller.events.GameEventListener;
import controller.events.GameUiModel;
import controller.api.GameStateListener;

/**
 * Orchestrates the lifecycle of a single game session and fans out state/events.
 */
public class GameSessionService implements TurnTimer.Listener {

    private final GameController gameController;
    private final TurnTimer turnTimer;

    private GameState currentGameState;
    private final List<GameStateListener> stateListeners = new ArrayList<>();
    private final List<GameEventListener> eventListeners = new ArrayList<>();

    public GameSessionService(GameController gameController, TurnTimer turnTimer) {
        this.gameController = gameController;
        this.turnTimer = turnTimer;
        this.turnTimer.addListener(this);
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

    GameState getCurrentGameState() {
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
        publish(GameEventKind.gameStarted, null);
        return currentGameState;
    }

    public GuessOutcome submitGuess(String guess) {
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

        // GameState is mutated in submitGuess; notify listeners with updated state.
        stateListeners.forEach(l -> l.onGameStateUpdate(currentGameState));
        publish(GameEventKind.gameStateUpdated, outcome);

        if (newStatus == GameStatus.finished) {
            publish(GameEventKind.gameFinished, outcome);
            turnTimer.stop();
        } else if (newStatus == GameStatus.awaitingWinnerKnowledge) {
            // Pause timers while waiting for winner knowledge response.
            turnTimer.stop();
        } else if (currentGameState.getConfig().timerDuration().isTimed() && nextTurn != null && nextTurn != player) {
            turnTimer.start(nextTurn);
        }
        return outcome;
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

        stateListeners.forEach(l -> l.onGameStateUpdate(currentGameState));

        if (after == GameStatus.finished || after == GameStatus.soloChase) {
            publish(GameEventKind.gameFinished, winnerKnewWord);
            turnTimer.stop();
        } else {
            publish(GameEventKind.gameStateUpdated, winnerKnewWord);
            if (after == GameStatus.waitingForFinalGuess
                    && currentGameState.getConfig().timerDuration().isTimed()
                    && currentGameState.getCurrentTurn() != null) {
                turnTimer.start(currentGameState.getCurrentTurn());
            }
        }
    }

    @Override
    public void onTimeUpdated(GamePlayer player, int remainingSeconds) {
        // Panels listen directly for UI updates; no domain action needed here.
    }

    @Override
    public void onTimeExpired(GamePlayer player) {
        if (currentGameState == null || player == null) {
            return;
        }
        if (!currentGameState.getConfig().timerDuration().isTimed()) {
            return;
        }
        if (currentGameState.getStatus() == GameStatus.finished) {
            return;
        }
        currentGameState.handleTimeout(player);
        turnTimer.stop();
        stateListeners.forEach(l -> l.onGameStateUpdate(currentGameState));
        publish(GameEventKind.timerExpired, player);
        publish(GameEventKind.gameFinished, null);
    }

    private void publish(GameEventKind kind, Object metadata) {
        var event = new GameEvent(kind, toUiModel(currentGameState), metadata);
        for (GameEventListener l : eventListeners) {
            l.onGameEvent(event);
        }
    }

    private GameUiModel toUiModel(GameState state) {
        if (state == null) return null;
        String playerOneName = name(state.getConfig().playerOne());
        String playerTwoName = name(state.getConfig().playerTwo());
        String winnerName = state.getWinner() == null ? null : name(state.getWinner());
        String provisional = state.getProvisionalWinner() == null ? null : name(state.getProvisionalWinner());
        Integer p1Remaining = turnTimer != null && state.getConfig().playerOne() != null
                ? turnTimer.getRemainingFor(state.getConfig().playerOne())
                : null;
        Integer p2Remaining = turnTimer != null && state.getConfig().playerTwo() != null
                ? turnTimer.getRemainingFor(state.getConfig().playerTwo())
                : null;
        int timerSeconds = state.getConfig().timerDuration() != null ? state.getConfig().timerDuration().seconds() : 0;
        String targetWord = null;
        if (state.getConfig().playerOne() != null && state.wordFor(state.getConfig().playerOne()) != null) {
            targetWord = state.wordFor(state.getConfig().playerOne()).word();
        }
        var guesses = state.getGuesses().stream()
                .map(g -> new controller.events.GuessView(
                        name(g.player()),
                        state.getConfig().playerOne() != null && g.player().equals(state.getConfig().playerOne()),
                        g.result()))
                .toList();
        var keyboard = buildKeyboardView(state);
        return new GameUiModel(
                state.getId(),
                state.getStatus(),
                state.getConfig().difficulty(),
                name(state.getCurrentTurn()),
                winnerName,
                provisional,
                playerOneName,
                playerTwoName,
                timerSeconds,
                p1Remaining,
                p2Remaining,
                List.copyOf(guesses),
                keyboard,
                targetWord
        );
    }

    private String name(model.GamePlayer player) {
        if (player == null || player.profile() == null || player.profile().username() == null) return null;
        return player.profile().username();
    }

    private controller.events.KeyboardView buildKeyboardView(GameState state) {
        // Basic keyboard aggregation: mark letters as used/present/correct based on guesses.
        var keyStates = new java.util.HashMap<Character, String>();
        state.getGuesses().forEach(g -> {
            var feedback = g.result().feedback();
            var guessWord = g.result().guess();
            for (int i = 0; i < guessWord.length() && i < feedback.size(); i++) {
                char c = Character.toUpperCase(guessWord.charAt(i));
                var fb = feedback.get(i);
                if (fb == null) continue;
                switch (fb) {
                    case correct -> keyStates.put(c, "correct");
                    case present -> keyStates.putIfAbsent(c, "present");
                    case notPresent -> keyStates.putIfAbsent(c, "absent");
                    default -> keyStates.putIfAbsent(c, "used");
                }
            }
        });
        return new controller.events.KeyboardView(java.util.Map.copyOf(keyStates));
    }
}
