package controller.events;

/**
 * Event emitted to listeners with an immutable UI-friendly snapshot.
 */
public record GameEvent(GameEventKind kind, GameUiModel view, TimerView timer) {
    public enum GameEventKind {
        gameStarted,
        gameStateUpdated,
        timerUpdated,
        gameFinished,
        timerExpired
    }

    public boolean hasView() {
        return view != null;
    }

    public boolean hasTimer() {
        return timer != null;
    }
}
