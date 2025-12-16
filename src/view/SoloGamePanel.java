package view;

import controller.GameController;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.Enums.Difficulty;
import model.Enums.TimerDuration;
import model.Enums.WordLength;
import model.Records.GamePlayer;
import model.Records.GuessResult;
import model.Records.PlayerProfile;

class SoloGamePanel extends JPanel {

    SoloGamePanel(Navigation navigation, GameController gameController) {
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

        var newGame = new JButton("New Word");
        newGame.addActionListener(e -> startNewGame());

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());

        controls.add(submit);
        controls.add(backspace);
        controls.add(newGame);
        controls.add(back);
        add(controls, BorderLayout.WEST);

        startNewGame();
    }

    private void handleGuess() {
        var rawGuess = guessField.getText();

        try {
            GuessResult result = gameController.submitGuess(player, rawGuess);
            grid.addGuessRow(new GuessRowPanel(result.guess(), result.feedback()));
            guessField.setText("");

            if (result.exactMatch()) {
                setStatus("You solved it!");
            } else {
                // This status message could be improved, but we'll leave it for now.
                setStatus(result.correctLetterCount() + " letters are correct.");
            }
        } catch (IllegalStateException | IllegalArgumentException ex) {
            // Exceptions from the controller are now used to display status messages.
            setStatus(ex.getMessage());
        }
    }

    private void handleBackspace() {
        var text = guessField.getText();
        if (text != null && !text.isEmpty()) {
            guessField.setText(text.substring(0, text.length() - 1));
        }
    }

    private void startNewGame() {
        grid.clearRows();
        guessField.setText("");
        setStatus(" ");
        var length = WordLength.five;
        var target = gameController.pickWord(length);
        gameController.startNewSoloGame(
                player,
                Difficulty.normal,
                length,
                TimerDuration.none,
                target
        );
    }

    private void setStatus(String text) {
        statusLabel.setText(text);
    }

    KeyboardPanel getKeyboardPanel() {
        return keyboardPanel;
    }

    private static final long serialVersionUID = 1L;

    private final GameController gameController;
    private final GamePlayer player;
    private final GuessGridPanel grid;
    private final KeyboardPanel keyboardPanel;
    private final JTextField guessField;
    private final JLabel statusLabel;
}
