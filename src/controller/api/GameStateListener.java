package controller.api;

import model.GameState;

public interface GameStateListener {
    void onGameStateUpdate(GameState newState);
}
