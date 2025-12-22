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
import controller.events.GameUiModel;
import controller.events.GameStatusView;
import controller.events.DifficultyView;

/**
 * Orchestrates the lifecycle of a single game session and fans out state/events.
 */
public class GameSessionService implements TurnTimer.Listener {

    private final GameController gameController;
    private final TurnTimer turnTimer;

    private GameState currentGameState;
    private final List<GameEventListener> eventListeners = new ArrayList<>();

    public GameSessionService(GameController gameController, TurnTimer turnTimer) {
        this.gameController = gameController;
        this.turnTimer = turnTimer;
        this.turnTimer.addListener(this);
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
        publish(GameEventKind.gameStateUpdated);
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
        var event = new GameEvent(kind, toUiModel(currentGameState));
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
                ? turnTimer.getRemainingFor(PlayerSlot.playerOne)
                : null;
        Integer p2Remaining = turnTimer != null && state.getConfig().playerTwo() != null
                ? turnTimer.getRemainingFor(PlayerSlot.playerTwo)
                : null;
        int timerSeconds = state.getConfig().timerDuration() != null ? state.getConfig().timerDuration().seconds() : 0;
        var guesses = state.getGuesses().stream()
                .map(g -> new controller.events.GuessView(
                        name(g.player()),
                        state.getConfig().playerOne() != null && g.player().equals(state.getConfig().playerOne()),
                        toView(g.result())))
                .toList();
        var keyboard = buildKeyboardView(state);
        return new GameUiModel(
                state.getId(),
                mapStatus(state.getStatus()),
                mapDifficulty(state.getConfig().difficulty()),
                name(state.getCurrentTurn()),
                winnerName,
                provisional,
                state.getWinnerKnewWord(),
                playerOneName,
                playerTwoName,
                timerSeconds,
                p1Remaining,
                p2Remaining,
                List.copyOf(guesses),
                keyboard
        );
    }

    private String name(model.GamePlayer player) {
        if (player == null || player.profile() == null || player.profile().username() == null) return null;
        return player.profile().username();
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

    private controller.events.KeyboardView buildKeyboardView(GameState state) {
        var keyStates = new java.util.HashMap<Character, String>();
        var difficulty = state.getConfig().difficulty();

        java.util.function.BiFunction<String, String, String> merge = (oldV, newV) -> {
            if (oldV == null) return newV;
            int oldS = strength(oldV);
            int newS = strength(newV);
            return newS > oldS ? newV : oldV;
        };

        state.getGuesses().forEach(g -> {
            var guess = g.result().guess();
            var feedback = g.result().feedback();

            if (difficulty == model.enums.Difficulty.expert) {
                for (int i = 0; i < guess.length(); i++) {
                    char c = Character.toUpperCase(guess.charAt(i));
                    keyStates.put(c, merge.apply(keyStates.get(c), "used"));
                }
                return;
            }

            int n = Math.min(guess.length(), feedback.size());
            for (int i = 0; i < n; i++) {
                char c = Character.toUpperCase(guess.charAt(i));
                var fb = feedback.get(i);
                if (fb == null) continue;

                String v = switch (fb) {
                    case correct -> "correct";
                    case present -> "present";
                    case notPresent -> "absent";
                    default -> "used";
                };

                keyStates.put(c, merge.apply(keyStates.get(c), v));
            }
        });

        return new controller.events.KeyboardView(java.util.Map.copyOf(keyStates));
    }

    private int strength(String v) {
        return switch (v) {
            case "correct" -> 3;
            case "present" -> 2;
            case "absent" -> 1;
            default -> 0; // "used"
        };
    }

    private controller.events.GuessResultView toView(model.GuessResult result) {
        var feedbackView = result.feedback().stream()
                .map(fb -> {
                    if (fb == null) return controller.events.LetterFeedbackView.unused;
                    return switch (fb) {
                        case correct -> controller.events.LetterFeedbackView.correct;
                        case present -> controller.events.LetterFeedbackView.present;
                        case notPresent -> controller.events.LetterFeedbackView.absent;
                        default -> controller.events.LetterFeedbackView.unused;
                    };
                })
                .toList();
        return new controller.events.GuessResultView(
                result.guess(),
                java.util.List.copyOf(feedbackView),
                result.correctLetterCount(),
                result.exactMatch()
        );
    }

    private GameStatusView mapStatus(GameStatus status) {
        if (status == null) return GameStatusView.setup;
        return switch (status) {
            case setup -> GameStatusView.setup;
            case inProgress -> GameStatusView.inProgress;
            case awaitingWinnerKnowledge -> GameStatusView.awaitingWinnerKnowledge;
            case waitingForFinalGuess -> GameStatusView.waitingForFinalGuess;
            case soloChase -> GameStatusView.soloChase;
            case finished -> GameStatusView.finished;
        };
    }

    private DifficultyView mapDifficulty(model.enums.Difficulty difficulty) {
        if (difficulty == null) return DifficultyView.normal;
        return switch (difficulty) {
            case normal -> DifficultyView.normal;
            case hard -> DifficultyView.hard;
            case expert -> DifficultyView.expert;
        };
    }
}
