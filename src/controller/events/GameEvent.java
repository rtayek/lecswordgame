package controller.events;

/**
 * Event emitted to listeners with an immutable UI-friendly snapshot.
 */
public record GameEvent(GameEventKind kind, GameUiModel view, Object metadata) {
    public enum GameEventKind {
        gameStarted,
        gameStateUpdated,
        gameFinished,
        timerExpired
    }
}
