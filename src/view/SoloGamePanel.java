package view;

import controller.GameController;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.Enums.Difficulty;
import model.Enums.GameMode;
import model.Enums.TimerDuration;
import model.Enums.WordLength;
import model.Enums.WordSource;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.GuessResult;
import model.Records.PlayerProfile;
import model.Records.WordChoice;

class SoloGamePanel extends JPanel {

    SoloGamePanel(Navigation navigation, GameController gameController) {
        this.navigation = navigation;
        this.gameController = gameController;
        this.player = new GamePlayer(new PlayerProfile("You", ""), true);

        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Solo Game"), BorderLayout.NORTH);
        
        grid = new GuessGridPanel();
        add(new JScrollPane(grid), BorderLayout.CENTER);

        guessField = new JTextField();
        statusLabel = new JLabel(" ");

        keyboardPanel = new KeyboardPanel(c -> guessField.setText(guessField.getText() + c));
        var bottom = new JPanel(new BorderLayout());
        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(guessField, BorderLayout.CENTER);
        bottom.add(keyboardPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        var controls = new JPanel();
        var submit = new JButton("Submit Guess");
        submit.addActionListener(e -> handleGuess());

        var backspace = new JButton("Backspace");
        backspace.addActionListener(e -> handleBackspace());

        var back = new JButton("Back to Setup");
        back.addActionListener(e -> navigation.showGameSetup());

        controls.add(submit);
        controls.add(backspace);
        controls.add(back);
        add(controls, BorderLayout.WEST);
    }

    public void onShow() {
        grid.clearRows();
        guessField.setText("");
        setStatus("New game started. Make your guess!");
    }
    
    private void handleGuess() {
        var rawGuess = guessField.getText();
        var gameState = navigation.getGameState();
        if (gameState == null) {
            setStatus("Please start a new game from the setup screen.");
            return;
        }

        try {
            var newGameState = gameController.submitGuess(gameState, player, rawGuess);
            navigation.setGameState(newGameState);

            // We need to get the latest guess from the state
            var guesses = newGameState.getGuesses();
            if (!guesses.isEmpty()) {
                GuessResult result = guesses.get(guesses.size() - 1).result();
                grid.addGuessRow(new GuessRowPanel(result.guess(), result.feedback()));
                guessField.setText("");

                if (result.exactMatch()) {
                    setStatus("You solved it!");
                } else {
                    setStatus(result.correctLetterCount() + " letters are correct.");
                }
            }
        } catch (IllegalStateException | IllegalArgumentException ex) {
            setStatus(ex.getMessage());
        }
    }
    
    private void handleBackspace() {
        var text = guessField.getText();
        if (text != null && !text.isEmpty()) {
            guessField.setText(text.substring(0, text.length() - 1));
        }
    }
    
    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    KeyboardPanel getKeyboardPanel() {
        return keyboardPanel;
    }

    private static final long serialVersionUID = 1L;

    private final Navigation navigation;
    private final GameController gameController;
    private final GamePlayer player;
    private final GuessGridPanel grid;
    private final KeyboardPanel keyboardPanel;
    private final JTextField guessField;
    private final JLabel statusLabel;
}
