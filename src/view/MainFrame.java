package view;

import controller.AppController;
import controller.GameController;
import controller.TimerController;
import util.PersistenceService;
import model.GameState;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.WordChoice;

import javax.swing.JFrame; // Missing Import
import javax.swing.JPanel; // Missing Import
import java.awt.CardLayout; // Missing Import

class MainFrame extends JFrame implements Navigation {

    public MainFrame(AppController appController, GameController gameController, TimerController timerController, PersistenceService persistenceService) {
        super("Word Guessing Game");
        this.gameController = gameController;
        this.timerController = timerController;
        this.persistenceService = persistenceService; // Initialize
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        var landing = new LandingPanel(this);
        var profile = new ProfileSetupPanel(this, appController); // Pass appController for profile loading/saving
        var instructions = new InstructionsPanel(this);
        var friends = new FriendsPanel(this);
        var gameLog = new GameLogPanel(this, appController); // Pass appController for game log
        var hardest = new HardestWordsPanel(this, persistenceService); // Pass persistenceService directly
        var setup = new GameSetupPanel(this, gameController);
        multiplayer = new MultiplayerGamePanel(this, gameController);
        solo = new SoloGamePanel(this, gameController);

        cards.add(landing, cardLanding);
        cards.add(profile, cardProfile);
        cards.add(instructions, cardInstructions);
        cards.add(friends, cardFriends);
        cards.add(gameLog, cardLog);
        cards.add(hardest, cardHardest);
        cards.add(setup, cardSetup);
        cards.add(multiplayer, cardMulti);
        cards.add(solo, cardSolo);

        add(cards);
        showLanding();
    }

    @Override
    public void showWordSelection(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, boolean isPlayerOneTurn) {
        this.pendingGameConfig = config;
        this.pendingPlayerOne = playerOne;
        this.pendingPlayerTwo = playerTwo;
        
        WordSelectionPanel wordSelectionPanel = new WordSelectionPanel(this, gameController, config, playerOne, playerTwo, isPlayerOneTurn);
        cards.add(wordSelectionPanel, cardWordSelection);
        layout.show(cards, cardWordSelection);
    }

    @Override
    public void playerOneWordSelected(WordChoice wordChoice) {
        this.playerOneChosenWord = wordChoice;
        if (pendingGameConfig.mode() == model.Enums.GameMode.solo) {
            // In solo mode, playerOne is the human guessing, playerTwo is computer whose word is chosen
            // The wordChoice here is the computer's word (for playerTwo)
            startGame(pendingGameConfig, pendingPlayerOne, pendingPlayerTwo, null, this.playerOneChosenWord);
        } else { // Multiplayer
            // Now ask Player Two to select their word
            showWordSelection(pendingGameConfig, pendingPlayerOne, pendingPlayerTwo, false);
        }
    }

    @Override
    public void playerTwoWordSelected(WordChoice wordChoice) {
        this.playerTwoChosenWord = wordChoice;
        startGame(pendingGameConfig, pendingPlayerOne, pendingPlayerTwo, this.playerOneChosenWord, this.playerTwoChosenWord);
    }

    private void startGame(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, WordChoice p1Word, WordChoice p2Word) {
        var state = gameController.startNewGame(config, p1Word, p2Word);
        setGameState(state);
        if (config.mode() == model.Enums.GameMode.multiplayer) {
            showMultiplayerGame();
        } else {
            showSoloGame();
        }
        // Clear pending states
        this.pendingGameConfig = null;
        this.pendingPlayerOne = null;
        this.pendingPlayerTwo = null;
        this.playerOneChosenWord = null;
        this.playerTwoChosenWord = null;
    }
    
    @Override
    public GameState getGameState() {
        return this.currentGameState;
    }

    @Override
    public void setGameState(GameState state) {
        this.currentGameState = state;
    }

    @Override
    public TimerController getTimerController() {
        return this.timerController;
    }

    @Override
    public void showLanding() {
        layout.show(cards, cardLanding);
    }

    @Override
    public void showProfileSetup() {
        layout.show(cards, cardProfile);
    }

    @Override
    public void showInstructions() {
        layout.show(cards, cardInstructions);
    }

    @Override
    public void showFriends() {
        layout.show(cards, cardFriends);
    }

    @Override
    public void showGameLog() {
        layout.show(cards, cardLog);
    }

    @Override
    public void showHardestWords() {
        layout.show(cards, cardHardest);
    }

    @Override
    public void showGameSetup() {
        layout.show(cards, cardSetup);
    }

    @Override
    public void showMultiplayerGame() {
    	multiplayer.onShow();
        layout.show(cards, cardMulti);
    }

    @Override
    public void showSoloGame() {
        solo.onShow();
        layout.show(cards, cardSolo);
    }

    private static final long serialVersionUID = 1L;

    private static final String cardLanding = "landing";
    private static final String cardProfile = "profile";
    private static final String cardInstructions = "instructions";
    private static final String cardFriends = "friends";
    private static final String cardLog = "log";
    private static final String cardHardest = "hardest";
    private static final String cardSetup = "setup";
    private static final String cardWordSelection = "wordSelection"; // New card constant
    private static final String cardMulti = "multiplayer";
    private static final String cardSolo = "solo";

    private final CardLayout layout = new CardLayout();
    private final JPanel cards = new JPanel(layout);
    private final MultiplayerGamePanel multiplayer;
    private final SoloGamePanel solo;
    private final TimerController timerController;
    private final GameController gameController; 
    private final PersistenceService persistenceService; // Correctly placed PersistenceService
    
    // For word selection flow
    private GameConfig pendingGameConfig;
    private GamePlayer pendingPlayerOne;
    private GamePlayer pendingPlayerTwo;
    private WordChoice playerOneChosenWord;
    private WordChoice playerTwoChosenWord;
    private GameState currentGameState;
}
