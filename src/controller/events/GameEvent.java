package controller.events;

import model.GameState;

public record GameEvent(GameEventKind kind, GameState snapshot, Object metadata) {
    public enum GameEventKind {
        gameStarted,
        gameStateUpdated,
        gameFinished,
        timerExpired
    }
}
