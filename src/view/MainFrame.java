package view;

import controller.AppController;
import controller.WordSelectionViewData;
import controller.api.Navigation;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

class MainFrame extends JFrame implements Navigation {
    private final AppController appController;

    public MainFrame(AppController appController) {
        super("Word Guessing Game");
        this.appController = appController;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new java.awt.Color(0x0D1B2A)); // deep navy backdrop for frame

        landingPanel = new LandingPanel(this);
        profilePanel = new ProfileSetupPanel(this, appController); // Pass appController for profile loading/saving
        instructionsPanel = new InstructionsPanel(this);
        friendsPanel = new FriendsPanel(this);
        gameLogPanel = new GameLogPanel(this, appController); // Pass appController for game log
        hardestWordsPanel = new HardestWordsPanel(this, appController); // Pass persistenceService directly
        setupPanel = new GameSetupPanel(this, appController);
        multiplayer = new MultiplayerGamePanel(this, appController);
        solo = new SoloGamePanel(this, appController);

        cards.add(landingPanel, cardLanding);
        cards.add(profilePanel, cardProfile);
        cards.add(instructionsPanel, cardInstructions);
        cards.add(friendsPanel, cardFriends);
        cards.add(gameLogPanel, cardLog);
        cards.add(hardestWordsPanel, cardHardest);
        cards.add(setupPanel, cardSetup);
        cards.add(multiplayer, cardMulti);
        cards.add(solo, cardSolo);

        add(cards);
        showLanding();
    }

    @Override
    public void showWordSelection(WordSelectionViewData data) {
        if (wordSelectionPanel == null) {
            wordSelectionPanel = new WordSelectionPanel(appController, data);
            cards.add(wordSelectionPanel, cardWordSelection);
        } else {
            wordSelectionPanel.setContext(data);
        }
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
        gameLogPanel.onShow();
        layout.show(cards, cardLog);
    }

    @Override
    public void showHardestWords() {
        hardestWordsPanel.onShow();
        layout.show(cards, cardHardest);
    }

    @Override
    public void showGameSetup() {
        layout.show(cards, cardSetup);
    }

    @Override
    public void showMultiplayerGame() {
        layout.show(cards, cardMulti);
    }

    @Override
    public void showSoloGame() {
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
    private final LandingPanel landingPanel;
    private final ProfileSetupPanel profilePanel;
    private final InstructionsPanel instructionsPanel;
    private final FriendsPanel friendsPanel;
    private final GameLogPanel gameLogPanel;
    private final HardestWordsPanel hardestWordsPanel;
    private final GameSetupPanel setupPanel;
    private WordSelectionPanel wordSelectionPanel;
}
