package view;

import controller.GameController;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.GamePlayer;
import model.Enums.GameStatus;

class MultiplayerGamePanel extends JPanel {

    MultiplayerGamePanel(Navigation navigation, GameController gameController) {
        this.gameController = gameController;

        setLayout(new BorderLayout(8, 8));

        currentPlayerLabel = new JLabel("Current player: (none)");
        add(currentPlayerLabel, BorderLayout.NORTH);

        var gridContainer = new JPanel(new GridLayout(1, 2, 10, 10));

        leftGrid = new GuessGridPanel();
        rightGrid = new GuessGridPanel();

        var leftPanel = new JPanel(new BorderLayout());
        playerOneLabel = new JLabel("Player 1");
        leftPanel.add(playerOneLabel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(leftGrid), BorderLayout.CENTER);

        var rightPanel = new JPanel(new BorderLayout());
        playerTwoLabel = new JLabel("Player 2");
        rightPanel.add(playerTwoLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(rightGrid), BorderLayout.CENTER);

        gridContainer.add(leftPanel);
        gridContainer.add(rightPanel);

        add(gridContainer, BorderLayout.CENTER);

        guessField = new JTextField(10);
        keyboardPanel = new KeyboardPanel(c -> {
            guessField.setText(guessField.getText() + c);
        });

        statusLabel = new JLabel(" ");
        var bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(statusLabel, BorderLayout.NORTH);
        bottomPanel.add(guessField, BorderLayout.CENTER);
        bottomPanel.add(keyboardPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        var controls = new JPanel();

        submitButton = new JButton("Submit Guess");
        submitButton.addActionListener(e -> handleGuess());

        var backspace = new JButton("Backspace");
        backspace.addActionListener(e -> handleBackspace());

        var enter = new JButton("Enter");
        enter.addActionListener(e -> handleGuess());

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());

        controls.add(submitButton);
        controls.add(backspace);
        controls.add(enter);
        controls.add(back);
        add(controls, BorderLayout.WEST);
    }

    private void handleGuess() {
        var state = gameController.getGameState();
        if (state == null) {
            setStatus("No active game. Start a multiplayer game first.");
            submitButton.setEnabled(false);
            return;
        }

        var wordLength = state.getWordLength();
        if (wordLength == null) {
            setStatus("Word length is not set for this game.");
            submitButton.setEnabled(false);
            return;
        }

        if (state.getStatus() == GameStatus.finished) {
            setStatus("Game finished.");
            submitButton.setEnabled(false);
            return;
        }

        GamePlayer current = state.getCurrentTurn();
        if (current == null) {
            setStatus("No current player.");
            submitButton.setEnabled(false);
            return;
        }

        var raw = guessField.getText();
        var upper = raw == null ? "" : raw.trim().toUpperCase();
        var guess = upper.replaceAll("[^A-Z]", "");
        if (guess.isEmpty()) {
            setStatus("Enter a guess first.");
            return;
        }

        var expectedLength = wordLength.length();
        if (guess.length() != expectedLength) {
            setStatus("Guess must be " + expectedLength + " letters.");
            return;
        }

        try {
            var result = gameController.submitGuess(current, guess);

            if (current == state.getPlayerOne()) {
                leftGrid.addGuessRow(new GuessRowPanel(result.getGuess(), result.getFeedback()));
            } else {
                rightGrid.addGuessRow(new GuessRowPanel(result.getGuess(), result.getFeedback()));
            }

            guessField.setText("");
            setStatus(" ");

            updateCurrentPlayerLabel();
            submitButton.setEnabled(true);

        } catch (RuntimeException ex) {
            setStatus("Error: " + ex.getMessage());
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

    private void updateCurrentPlayerLabel() {
        var state = gameController.getGameState();
        if (state == null) {
            currentPlayerLabel.setText("Current player: (none)");
            submitButton.setEnabled(false);
            return;
        }

        if (state.getStatus() == GameStatus.finished) {
            currentPlayerLabel.setText("Game finished.");
            submitButton.setEnabled(false);
            return;
        }

        var player = state.getCurrentTurn();
        if (player == null || player.getProfile() == null) {
            currentPlayerLabel.setText("Current player: (none)");
            submitButton.setEnabled(false);
            return;
        }

        var name = player.getProfile().getUsername();
        if (name == null || name.isBlank()) {
            name = player == state.getPlayerOne() ? "Player 1" : "Player 2";
        }

        currentPlayerLabel.setText("Current player: " + name);
        submitButton.setEnabled(true);
    }

    void onShow() {
        var state = gameController.getGameState();
        leftGrid.clearRows();
        rightGrid.clearRows();
        setStatus(" ");

        if (state != null) {
            var p1 = state.getPlayerOne();
            var p2 = state.getPlayerTwo();

            var name1 = p1 != null && p1.getProfile() != null ? p1.getProfile().getUsername() : null;
            var name2 = p2 != null && p2.getProfile() != null ? p2.getProfile().getUsername() : null;

            if (name1 == null || name1.isBlank()) {
                name1 = "Player 1";
            }
            if (name2 == null || name2.isBlank()) {
                name2 = "Player 2";
            }

            playerOneLabel.setText(name1);
            playerTwoLabel.setText(name2);
        } else {
            playerOneLabel.setText("Player 1");
            playerTwoLabel.setText("Player 2");
        }

        updateCurrentPlayerLabel();
    }

    private final GameController gameController;
    private final JLabel currentPlayerLabel;
    private final GuessGridPanel leftGrid;
    private final GuessGridPanel rightGrid;
    private final KeyboardPanel keyboardPanel;
    private final JTextField guessField;
    private final JLabel playerOneLabel;
    private final JLabel playerTwoLabel;
    private final JLabel statusLabel;
    private final JButton submitButton;

    private static final long serialVersionUID = 1L;
}
