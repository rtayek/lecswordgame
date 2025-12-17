package view;

import model.GameState;

interface Navigation {
    GameState getGameState();

    void setGameState(GameState state);

    void showLanding();

    void showProfileSetup();

    void showInstructions();

    void showFriends();

    void showGameLog();

    void showHardestWords();

    void showGameSetup();

    void showMultiplayerGame();

    void showSoloGame();
}
