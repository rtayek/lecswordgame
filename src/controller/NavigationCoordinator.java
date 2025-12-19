package controller;

import model.GameState.GameConfig;
import model.GamePlayer;
import controller.api.Navigation;

/**
 * Thin wrapper around Navigation to centralize UI transitions.
 */
public class NavigationCoordinator {

    private Navigation navigation;

    public void setNavigation(Navigation navigation) {
        this.navigation = navigation;
    }

    public Navigation getNavigation() {
        return navigation;
    }

    public void showLanding() {
        if (navigation != null) navigation.showLanding();
    }

    public void showProfileSetup() {
        if (navigation != null) navigation.showProfileSetup();
    }

    public void showInstructions() {
        if (navigation != null) navigation.showInstructions();
    }

    public void showFriends() {
        if (navigation != null) navigation.showFriends();
    }

    public void showGameLog() {
        if (navigation != null) navigation.showGameLog();
    }

    public void showHardestWords() {
        if (navigation != null) navigation.showHardestWords();
    }

    public void showGameSetup() {
        if (navigation != null) navigation.showGameSetup();
    }

    public void showWordSelection(WordSelectionViewData data) {
        if (navigation != null) navigation.showWordSelection(data);
    }

    public void showMultiplayerGame() {
        if (navigation != null) navigation.showMultiplayerGame();
    }

    public void showSoloGame() {
        if (navigation != null) navigation.showSoloGame();
    }
}
