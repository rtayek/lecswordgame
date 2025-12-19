package controller.events;

public record GameEvent(GameEventKind kind, GameView view, Object metadata) {
    public enum GameEventKind {
        gameStarted,
        gameStateUpdated,
        gameFinished,
        timerExpired
    }
}
