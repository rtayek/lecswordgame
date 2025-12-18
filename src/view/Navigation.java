package view;

import controller.TimerController;
import model.GameState;
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
    
    controller.TurnTimer getTimerController();

}
