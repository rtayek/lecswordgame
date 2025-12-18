package controller;

import java.util.ArrayList;
import java.util.List;
import model.GameState;
import model.GameState.GameConfig;
import model.Records.GameLogEntry;
import model.Records.GamePlayer;
import model.Records.GuessOutcome;
import model.Records.HardWordEntry;
import model.Records.PlayerProfile;
import model.Records.WordChoice;
import model.enums.GameMode;
import model.enums.WordLength;
import util.PersistenceService;
import view.Navigation;
import controller.events.GameEvent;
import controller.events.GameEventListener;
import view.listeners.GameStateListener;

public class AppController {

    private final GameSessionService gameSessionService;
    private final WordSelectionFlow wordSelectionFlow = new WordSelectionFlow();
    private final ProfileService profileService;
    private final NavigationCoordinator navigationCoordinator = new NavigationCoordinator();
    private final TurnTimer turnTimer;

    public AppController(PersistenceService persistenceService, GameController gameController, TurnTimer turnTimer) {
        this.gameSessionService = new GameSessionService(gameController, turnTimer);
        this.profileService = new ProfileService(persistenceService);
        this.turnTimer = turnTimer;
    }
    
    public void addGameStateListener(GameStateListener listener) {
        gameSessionService.addStateListener(listener);
    }
    
    public void addGameEventListener(GameEventListener listener) {
        gameSessionService.addEventListener(listener);
    }
    
    public void setNavigation(Navigation navigation) {
        navigationCoordinator.setNavigation(navigation);
    }

    public GameState getGameState() {
        return gameSessionService.getCurrentGameState();
    }

    public void requestNewGame(GameConfig config) {
        wordSelectionFlow.start(config);
        navigationCoordinator.showWordSelection(config, config.playerOne(), config.playerTwo(), true);
    }
    
    public void playerOneWordSelected(WordChoice wordChoice) {
        var startRequest = wordSelectionFlow.playerOneSelected(wordChoice);
        if (startRequest != null) {
            startGame(startRequest.config(), startRequest.playerOne(), startRequest.playerTwo(), startRequest.playerOneWord(), startRequest.playerTwoWord());
        } else {
            var cfg = wordSelectionFlow.getPendingConfig();
            navigationCoordinator.showWordSelection(cfg, wordSelectionFlow.getPendingPlayerOne(), wordSelectionFlow.getPendingPlayerTwo(), false);
        }
    }

    public void playerTwoWordSelected(WordChoice wordChoice) {
        var startRequest = wordSelectionFlow.playerTwoSelected(wordChoice);
        startGame(startRequest.config(), startRequest.playerOne(), startRequest.playerTwo(), startRequest.playerOneWord(), startRequest.playerTwoWord());
    }

    private void startGame(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, WordChoice p1Word, WordChoice p2Word) {
        var state = gameSessionService.startNewGame(config, p1Word, p2Word);

        if (config.mode() == GameMode.multiplayer) {
            navigationCoordinator.showMultiplayerGame();
        } else {
            navigationCoordinator.showSoloGame();
        }
        // Clear pending states
        wordSelectionFlow.clear();
    }
    
    public GuessOutcome submitGuess(String guess) {
        if (gameSessionService.getCurrentGameState() == null) {
            throw new IllegalStateException("Start a new game first.");
        }
        return gameSessionService.submitGuess(guess);
    }

    public PlayerProfile getCurrentProfile() {
        return profileService.getCurrentProfile();
    }

    public void setCurrentProfile(PlayerProfile currentProfile) {
        profileService.saveProfile(currentProfile);
    }

    public List<GameLogEntry> getGameLog() {
        return profileService.getGameLogs();
    }

    public void addGameLogEntry(GameLogEntry entry) {
        profileService.addGameLogEntry(entry);
    }
    
    public List<HardWordEntry> getHardestWords() {
        return profileService.getHardestWords();
    }

    public String pickWord(WordLength length) {
        return gameSessionService.pickWord(length);
    }

    public boolean isValidWord(String word, WordLength length) {
        return gameSessionService.isValidWord(word, length);
    }

    public void reportWinnerKnowledge(boolean winnerKnewWord) {
        gameSessionService.applyWinnerKnowledge(winnerKnewWord);
    }

    public TurnTimer getTurnTimer() {
        return turnTimer;
    }
}
