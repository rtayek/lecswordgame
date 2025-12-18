package view;

import controller.TimerController;
import model.GameState;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.WordChoice;

public interface Navigation {

    void showLanding();



    void showProfileSetup();



    void showInstructions();



    void showFriends();



    void showGameLog();



    void showHardestWords();



    void showGameSetup();



    void showWordSelection(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, boolean isPlayerOneTurn);



    void showMultiplayerGame();



    void showSoloGame();
    
    controller.TurnTimer getTimerController();

}
