package view.listeners;

import model.GameState;

public interface GameStateListener {
    void onGameStateUpdate(GameState newState);
}
