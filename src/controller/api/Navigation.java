package controller.api;

import controller.WordSelectionViewData;

public interface Navigation {
    void showLanding();
    void showProfileSetup();
    void showInstructions();
    void showFriends();
    void showGameLog();
    void showHardestWords();
    void showGameSetup();
    void showWordSelection(WordSelectionViewData data);
    void showMultiplayerGame();
    void showSoloGame();
}
