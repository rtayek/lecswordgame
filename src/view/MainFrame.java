package view;

import controller.AppController;
import controller.GameController;
import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import model.GameState;

public class MainFrame extends JFrame implements Navigation {

    public MainFrame(AppController appController, GameController gameController) {
        super("Word Guessing Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        var landing = new LandingPanel(this);
        var profile = new ProfileSetupPanel(this);
        var instructions = new InstructionsPanel(this);
        var friends = new FriendsPanel(this);
        var gameLog = new GameLogPanel(this);
        var hardest = new HardestWordsPanel(this);
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
    public GameState getGameState() {
        return this.currentGameState;
    }

    @Override
    public void setGameState(GameState state) {
        this.currentGameState = state;
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
    private static final String cardMulti = "multiplayer";
    private static final String cardSolo = "solo";

    private final CardLayout layout = new CardLayout();
    private final JPanel cards = new JPanel(layout);
    private final MultiplayerGamePanel multiplayer;
    private final SoloGamePanel solo;
    private GameState currentGameState;
}
