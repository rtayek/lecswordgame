package controller;

import controller.events.DifficultyView;
import controller.events.GameStatusView;
import controller.events.GameUiModel;
import controller.events.GuessResultView;
import controller.events.GuessView;
import controller.events.KeyboardView;
import controller.events.LetterFeedbackView;
import controller.events.PlayerSlot;
import controller.events.FinishStateView;
import controller.events.FinishReasonView;
import java.util.List;
import model.GamePlayer;
import model.GameState;
import model.enums.GameStatus;
import model.enums.FinishState;

/**
 * Maps domain game state to view-friendly snapshots.
 */
class GameUiModelMapper {

    private final TurnTimer turnTimer;
    private final KeyboardViewBuilder keyboardBuilder;

    GameUiModelMapper(TurnTimer turnTimer, KeyboardViewBuilder keyboardBuilder) {
        this.turnTimer = turnTimer;
        this.keyboardBuilder = keyboardBuilder;
    }

    GameUiModel toUiModel(GameState state) {
        return toUiModel(state, null);
    }

    GameUiModel toUiModel(GameState state, FinishReasonView finishReason) {
        if (state == null) return null;
        var config = state.getConfig();
        String playerOneName = name(config.playerOne());
        String playerTwoName = name(config.playerTwo());
        String winnerName = state.getWinner() == null ? null : name(state.getWinner());
        String provisional = state.getProvisionalWinner() == null ? null : name(state.getProvisionalWinner());

        boolean timed = config.timerDuration() != null && config.timerDuration().isTimed();
        Integer p1Remaining = (turnTimer != null && config.playerOne() != null && timed)
                ? turnTimer.getRemainingFor(PlayerSlot.playerOne)
                : null;
        Integer p2Remaining = (turnTimer != null && config.playerTwo() != null && timed)
                ? turnTimer.getRemainingFor(PlayerSlot.playerTwo)
                : null;
        int timerSeconds = config.timerDuration() != null ? config.timerDuration().seconds() : 0;
        var guesses = state.getGuesses().stream()
                .map(g -> new GuessView(
                        name(g.player()),
                        config.playerOne() != null && g.player().equals(config.playerOne()),
                        toView(g.result())))
                .toList();
        KeyboardView keyboard = keyboardBuilder.build(state);

        return new GameUiModel(
                state.getId(),
                mapStatus(state.getStatus()),
                mapDifficulty(config.difficulty()),
                name(state.getCurrentTurn()),
                winnerName,
                provisional,
                state.getWinnerKnewWord(),
                mapFinishState(state.getPlayerFinishState(config.playerOne())),
                mapFinishState(state.getPlayerFinishState(config.playerTwo())),
                finishReason,
                playerOneName,
                playerTwoName,
                timerSeconds,
                p1Remaining,
                p2Remaining,
                List.copyOf(guesses),
                keyboard
        );
    }

    private String name(GamePlayer player) {
        if (player == null || player.profile() == null || player.profile().username() == null) return null;
        return player.profile().username();
    }

    private GuessResultView toView(model.GuessResult result) {
        var feedbackView = result.feedback().stream()
                .map(fb -> {
                    if (fb == null) return LetterFeedbackView.unused;
                    return switch (fb) {
                        case correct -> LetterFeedbackView.correct;
                        case present -> LetterFeedbackView.present;
                        case notPresent -> LetterFeedbackView.absent;
                        default -> LetterFeedbackView.unused;
                    };
                })
                .toList();
        return new GuessResultView(
                result.guess(),
                List.copyOf(feedbackView),
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

    private FinishStateView mapFinishState(FinishState state) {
        if (state == null) return FinishStateView.notFinished;
        return switch (state) {
            case notFinished -> FinishStateView.notFinished;
            case finishedSuccess -> FinishStateView.finishedSuccess;
            case finishedFail -> FinishStateView.finishedFail;
        };
    }
}
