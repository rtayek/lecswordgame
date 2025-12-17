package view;

import controller.TimerController;
import model.GameState;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.WordChoice;

interface Navigation {
    GameState getGameState();

    void setGameState(GameState state);

    TimerController getTimerController();

    void showLanding();
//...

    void showProfileSetup();

    void showInstructions();

    void showFriends();

    void showGameLog();

    void showHardestWords();

    void showGameSetup();

    void showWordSelection(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, boolean isPlayerOneTurn);

    void playerOneWordSelected(WordChoice wordChoice);

    void playerTwoWordSelected(WordChoice wordChoice);

    void showMultiplayerGame();

    void showSoloGame();
}
