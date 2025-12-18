package view.listeners;

import model.GameState;

public interface GameEventListener {
    void onGameStart(GameState initialState);
    void onGameOver(GameState finalState);
}
