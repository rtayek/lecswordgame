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
    private final AppController appController;

    public MainFrame(AppController appController, GameController gameController, TimerController timerController, PersistenceService persistenceService) {
        super("Word Guessing Game");
        this.appController = appController;
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
        var hardest = new HardestWordsPanel(this, appController); // Pass persistenceService directly
        var setup = new GameSetupPanel(this, appController);
        multiplayer = new MultiplayerGamePanel(this, appController, timerController);
        solo = new SoloGamePanel(this, appController, timerController);

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
        WordSelectionPanel wordSelectionPanel = new WordSelectionPanel(appController, gameController, config, playerOne, playerTwo, isPlayerOneTurn);
        cards.add(wordSelectionPanel, cardWordSelection);
        layout.show(cards, cardWordSelection);
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
    private final GameController gameController;
    private final TimerController timerController;
    private final PersistenceService persistenceService;
}
