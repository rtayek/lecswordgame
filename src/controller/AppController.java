package controller;

import java.util.ArrayList;
import java.util.List;
import model.GameState.GameConfig;
import model.GameLogEntry;
import model.GamePlayer;
import model.GuessOutcome;
import model.HardWordEntry;
import model.PlayerProfile;
import model.WordChoice;
import model.enums.GameMode;
import model.enums.WordLength;
import util.PersistenceService;
import controller.api.Navigation;
import controller.events.GameEvent;
import controller.events.GameEventListener;
import controller.api.GameStateListener;

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

    public void requestNewGame(GameConfig config) {
        wordSelectionFlow.start(config);
        navigationCoordinator.showWordSelection(buildWordSelectionData(config, true));
    }
    
    public void playerOneWordSelected(WordChoice wordChoice) {
        var startRequest = wordSelectionFlow.playerOneSelected(wordChoice);
        if (startRequest != null) {
            startGame(startRequest.config(), startRequest.playerOne(), startRequest.playerTwo(), startRequest.playerOneWord(), startRequest.playerTwoWord());
        } else {
            var cfg = wordSelectionFlow.getPendingConfig();
            navigationCoordinator.showWordSelection(buildWordSelectionData(cfg, false));
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

    public WordSelectionViewData buildWordSelectionData(GameConfig config, boolean isPlayerOneTurn) {
        if (config == null) return null;
        var opponent = isPlayerOneTurn ? config.playerTwo() : config.playerOne();
        var name = opponent != null && opponent.profile() != null ? opponent.profile().username() : "Player";
        boolean isMultiplayer = config.mode() == GameMode.multiplayer;
        return new WordSelectionViewData(name, config.wordLength().length(), isMultiplayer, isPlayerOneTurn);
    }

    public void startMultiplayerGame(String playerOneName, String playerTwoName, model.enums.Difficulty difficulty,
                                     model.enums.WordLength wordLength, model.enums.TimerDuration timer) {
        var p1 = new model.GamePlayer(new model.PlayerProfile(playerOneName, ""), true);
        var p2 = new model.GamePlayer(new model.PlayerProfile(playerTwoName, ""), true);
        var config = new GameConfig(model.enums.GameMode.multiplayer, difficulty, wordLength, timer, p1, p2);
        requestNewGame(config);
    }

    public void startSoloGame(String playerName, model.enums.Difficulty difficulty,
                              model.enums.WordLength wordLength, model.enums.TimerDuration timer) {
        var human = new model.GamePlayer(new model.PlayerProfile(playerName, ""), true);
        var cpu = new model.GamePlayer(new model.PlayerProfile("Computer", ""), false);
        var config = new GameConfig(model.enums.GameMode.solo, difficulty, wordLength, timer, human, cpu);
        requestNewGame(config);
    }
}
