package controller;

import java.util.ArrayList;
import java.util.List;
import model.GameState;
import model.GameState.GameConfig;
import model.Records.GameLogEntry;
import model.Records.GamePlayer;
import model.Records.HardWordEntry;
import model.Records.PlayerProfile;
import model.Records.WordChoice;
import util.PersistenceService;
import view.Navigation;
import view.listeners.GameEventListener;
import view.listeners.GameStateListener;

public class AppController {

    private final PersistenceService persistenceService;
    private final GameController gameController;
    private final TimerController timerController;
    private Navigation navigation;
    private GameState currentGameState;
    
    // For word selection flow
    private GameConfig pendingGameConfig;
    private GamePlayer pendingPlayerOne;
    private GamePlayer pendingPlayerTwo;
    private WordChoice playerOneChosenWord;
    private WordChoice playerTwoChosenWord;
    
    private final List<GameStateListener> gameStateListeners = new ArrayList<>();
    private final List<GameEventListener> gameEventListeners = new ArrayList<>();
    
    PlayerProfile currentProfile;
    final List<GameLogEntry> gameLog;

    public AppController(PersistenceService persistenceService, GameController gameController, TimerController timerController) {
        this.persistenceService = persistenceService;
        this.gameController = gameController;
        this.timerController = timerController;
        this.currentProfile = persistenceService.loadPlayerProfile(); // Load on startup
        this.gameLog = persistenceService.loadGameLogs(); // Load on startup
    }
    
    public void addGameStateListener(GameStateListener listener) {
        gameStateListeners.add(listener);
    }
    
    public void addGameEventListener(GameEventListener listener) {
        gameEventListeners.add(listener);
    }
    
    public void setNavigation(Navigation navigation) {
        this.navigation = navigation;
    }

    public GameState getGameState() {
        return currentGameState;
    }

    public void setGameState(GameState gameState) {
        this.currentGameState = gameState;
        gameStateListeners.forEach(l -> l.onGameStateUpdate(gameState));
    }

    public void requestNewGame(GameConfig config) {
        this.pendingGameConfig = config;
        this.pendingPlayerOne = config.playerOne();
        this.pendingPlayerTwo = config.playerTwo();
        navigation.showWordSelection(config, config.playerOne(), config.playerTwo(), true);
    }
    
    public void playerOneWordSelected(WordChoice wordChoice) {
        this.playerOneChosenWord = wordChoice;
        if (pendingGameConfig.mode() == model.Enums.GameMode.solo) {
            // In solo mode, playerOne is the human guessing, playerTwo is computer whose word is chosen
            // The wordChoice here is the computer's word (for playerTwo)
            startGame(pendingGameConfig, pendingPlayerOne, pendingPlayerTwo, null, this.playerOneChosenWord);
        } else { // Multiplayer
            // Now ask Player Two to select their word
            navigation.showWordSelection(pendingGameConfig, pendingPlayerOne, pendingPlayerTwo, false);
        }
    }

    public void playerTwoWordSelected(WordChoice wordChoice) {
        this.playerTwoChosenWord = wordChoice;
        startGame(pendingGameConfig, pendingPlayerOne, pendingPlayerTwo, this.playerOneChosenWord, this.playerTwoChosenWord);
    }

    private void startGame(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, WordChoice p1Word, WordChoice p2Word) {
        var state = gameController.startNewGame(config, p1Word, p2Word);
        setGameState(state);
        
        gameEventListeners.forEach(l -> l.onGameStart(state));
        
        if (config.mode() == model.Enums.GameMode.multiplayer) {
            navigation.showMultiplayerGame();
        } else {
            navigation.showSoloGame();
        }
        // Clear pending states
        this.pendingGameConfig = null;
        this.pendingPlayerOne = null;
        this.pendingPlayerTwo = null;
        this.playerOneChosenWord = null;
        this.playerTwoChosenWord = null;
    }
    
    public void submitGuess(String guess) {
        if (currentGameState == null) {
            return;
        }
        
        var player = currentGameState.getCurrentTurn();
        var outcome = gameController.submitGuess(currentGameState, player, guess);
        
        // The GameState is mutated by submitGuess, so we just need to notify listeners.
        setGameState(currentGameState);
        
        if (currentGameState.getStatus() == model.Enums.GameStatus.finished) {
            gameEventListeners.forEach(l -> l.onGameOver(currentGameState));
        }
    }

    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(PlayerProfile currentProfile) {
        this.currentProfile = currentProfile;
        persistenceService.savePlayerProfile(currentProfile); // Save immediately
    }

    public List<GameLogEntry> getGameLog() {
        return new ArrayList<>(gameLog);
    }

    public void addGameLogEntry(GameLogEntry entry) {
        gameLog.add(entry);
        persistenceService.saveGameLogs(gameLog); // Save immediately
    }
    
    public List<HardWordEntry> getHardestWords() {
        return persistenceService.loadHardestWords();
    }
}
