package view;

import controller.GameController;
import controller.TimerController;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.Records.GamePlayer;
import model.Enums.*;
import model.GameState;

import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import model.GameState.GameConfig;
import model.Records.WordChoice;
import model.Enums.FinishState;
import model.Enums.GameMode;
import util.SoundEffect;
import util.ResourceLoader;

class MultiplayerGamePanel extends JPanel implements TimerController.Listener {

    MultiplayerGamePanel(Navigation navigation, GameController gameController) {
        this.navigation = navigation;
        this.gameController = gameController;
        navigation.getTimerController().setListener(this);

        setLayout(new BorderLayout(8, 8));

        currentPlayerLabel = new JLabel("Current player: (none)");
        add(currentPlayerLabel, BorderLayout.NORTH);

        var gridContainer = new JPanel(new GridLayout(1, 2, 10, 10));

        leftGrid = new GuessGridPanel();
        rightGrid = new GuessGridPanel();

        var leftPanel = new JPanel(new BorderLayout());
        playerOneLabel = new JLabel("Player 1");
        playerOneTimerLabel = new JLabel("00:00");
        var leftHeader = new JPanel(new BorderLayout());
        leftHeader.add(playerOneLabel, BorderLayout.WEST);
        leftHeader.add(playerOneTimerLabel, BorderLayout.EAST);
        leftPanel.add(leftHeader, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(leftGrid), BorderLayout.CENTER);

        var rightPanel = new JPanel(new BorderLayout());
        playerTwoLabel = new JLabel("Player 2");
        playerTwoTimerLabel = new JLabel("00:00");
        var rightHeader = new JPanel(new BorderLayout());
        rightHeader.add(playerTwoLabel, BorderLayout.WEST);
        rightHeader.add(playerTwoTimerLabel, BorderLayout.EAST);
        rightPanel.add(rightHeader, BorderLayout.NORTH);
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
        GuessUIHelper.Outcome outcome = GuessUIHelper.submitGuess(
                navigation,
                gameController,
                gs -> gs.getCurrentTurn(),
                guessField.getText(),
                "No active game. Start a multiplayer game first."
        );

        if (outcome.isError()) {
            setStatus(outcome.errorMessage());
            submitButton.setEnabled(false);
            return;
        }

        var state = outcome.state();
        var result = outcome.result();
        var difficulty = state.getConfig().difficulty();

        if (outcome.player() == state.getConfig().playerOne()) {
            leftGrid.addGuessRow(new GuessRowPanel(result, difficulty));
        } else {
            rightGrid.addGuessRow(new GuessRowPanel(result, difficulty));
        }

        guessField.setText("");
        setStatus(" ");
        
        updateCurrentPlayerLabel(state); // Update with state from outcome

        var finished = state.getStatus() == GameStatus.finished;
        submitButton.setEnabled(!finished);
        guessField.setEnabled(!finished);
        keyboardPanel.setEnabled(!finished); // Also disable keyboard

        if (state.getStatus() == GameStatus.finished || state.getStatus() == GameStatus.waitingForFinalGuess) {
            onGameEnd(state);
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

    private void updateCurrentPlayerLabel(GameState state) {
        if (state == null) {
            currentPlayerLabel.setText("Current player: (none)");
            submitButton.setEnabled(false);
            guessField.setEnabled(false); // Also disable guess field
            keyboardPanel.setEnabled(false); // Also disable keyboard
            return;
        }

        if (state.getStatus() == GameStatus.finished) {
            var winner = state.getWinner();
            if (winner == null) {
                currentPlayerLabel.setText("Game finished: It's a Tie!");
            } else {
                var name = winner.profile() != null ? winner.profile().username() : null;
                if (name == null || name.isBlank()) {
                    name = winner == state.getConfig().playerOne() ? "Player 1" : "Player 2";
                }
                currentPlayerLabel.setText(name + " wins!");
            }
            submitButton.setEnabled(false);
            guessField.setEnabled(false);
            keyboardPanel.setEnabled(false);
            return;
        }

        var player = state.getCurrentTurn();
        if (player == null || player.profile() == null) {
            currentPlayerLabel.setText("Current player: (none)");
            submitButton.setEnabled(false);
            guessField.setEnabled(false); // Also disable guess field
            keyboardPanel.setEnabled(false); // Also disable keyboard
            return;
        }

        var name = player.profile().username();
        if (name == null || name.isBlank()) {
            name = player == state.getConfig().playerOne() ? "Player 1" : "Player 2";
        }

        currentPlayerLabel.setText("Current player: " + name);
        submitButton.setEnabled(true);
        guessField.setEnabled(true); // Re-enable if game is not finished
        keyboardPanel.setEnabled(true); // Re-enable if game is not finished
    }

    void onShow() {
        var state = navigation.getGameState();
        leftGrid.clearRows();
        rightGrid.clearRows();
        setStatus(" ");

        if (state != null) {
            var p1 = state.getConfig().playerOne();
            var p2 = state.getConfig().playerTwo();

            var name1 = p1 != null && p1.profile() != null ? p1.profile().username() : null;
            var name2 = p2 != null && p2.profile() != null ? p2.profile().username() : null;

            if (name1 == null || name1.isBlank()) {
                name1 = "Player 1";
            }
            if (name2 == null || name2.isBlank()) {
                name2 = "Player 2";
            }

            playerOneLabel.setText(name1);
            playerTwoLabel.setText(name2);

            int p1Time = navigation.getTimerController().getRemainingFor(p1);
            int p2Time = navigation.getTimerController().getRemainingFor(p2);
            updateTimerLabel(playerOneTimerLabel, p1Time);
            updateTimerLabel(playerTwoTimerLabel, p2Time);

        } else {
            playerOneLabel.setText("Player 1");
            playerTwoLabel.setText("Player 2");
            updateTimerLabel(playerOneTimerLabel, 0);
            updateTimerLabel(playerTwoTimerLabel, 0);
        }

        updateCurrentPlayerLabel(state);
    }

    @Override
    public void onTimeUpdated(GamePlayer player, int remainingSeconds) {
        var state = navigation.getGameState();
        if (state == null) return;
        
        JLabel labelToUpdate = (player == state.getConfig().playerOne()) ? playerOneTimerLabel : playerTwoTimerLabel;
        updateTimerLabel(labelToUpdate, remainingSeconds);
    }

    @Override
    public void onTimeExpired(GamePlayer player) {
        var state = navigation.getGameState();
        if (state == null) return;

        String name = player.profile().username();
        if (name == null || name.isBlank()) {
            name = player == state.getConfig().playerOne() ? "Player 1" : "Player 2";
        }
        setStatus(name + " ran out of time!");
    }

    private void updateTimerLabel(JLabel label, int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));

        // Determine if timer should be red
        boolean isTimerRed = false;
        var gameState = navigation.getGameState();
        if (gameState != null && gameState.getConfig().timerDuration().isTimed()) {
            int gameDuration = gameState.getConfig().timerDuration().seconds();
            if (gameDuration >= 3 * 60 && totalSeconds < 60) { // 3-5 minute games, under 1 minute
                isTimerRed = true;
            } else if (gameDuration == 1 * 60 && totalSeconds < 30) { // 1 minute game, under 30 seconds
                isTimerRed = true;
            }
        }
        label.setForeground(isTimerRed ? Color.RED : Color.BLACK);
    }

    private final Navigation navigation;
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
    private final JLabel playerOneTimerLabel;
    private final JLabel playerTwoTimerLabel;

    private void onGameEnd(GameState state) {
        if (state.getStatus() == GameStatus.waitingForFinalGuess) {
            GamePlayer lastGuesser = state.getOpponent(state.getCurrentTurn()); // The one who just guessed correctly
            GamePlayer opponent = state.getCurrentTurn(); // The one who gets last chance

            JOptionPane.showMessageDialog(
                this,
                "%s guessed the word! %s, you get one last chance to guess %s's word.".formatted(
                    lastGuesser.profile().username(),
                    opponent.profile().username(),
                    lastGuesser.profile().username() // Whose word the opponent needs to guess
                ),
                "Last Chance!",
                JOptionPane.INFORMATION_MESSAGE
            );
            submitButton.setEnabled(true);
            guessField.setEnabled(true);
            keyboardPanel.setEnabled(true); // Re-enable keyboard
            // The turn has already been switched by GameState.applyGuessResult
            updateCurrentPlayerLabel(state); // Pass state
            return;
        }

        // GameStatus is finished
        String message;
        SoundEffect soundEffect;
        String graphicFile;
        GamePlayer winner = state.getWinner();
        GamePlayer playerOne = state.getConfig().playerOne();
        GamePlayer playerTwo = state.getConfig().playerTwo();

        if (winner == null) { // Tie
            soundEffect = SoundEffect.TIE;
            graphicFile = "tie.png"; // Assuming tie graphic
            message = "It's a Tie! Both players guessed the word.";
        } else if (winner.equals(playerOne) || winner.equals(playerTwo)) { // A player won
            soundEffect = SoundEffect.WIN;
            graphicFile = "win.png";
            
            GamePlayer winningPlayer = winner;
            GamePlayer losingPlayer = (winner.equals(playerOne)) ? playerTwo : playerOne;
            
            // "Did you know this word?" prompt for the winner
            int choice = JOptionPane.showConfirmDialog(
                this,
                "%s, you guessed the word! Did you know this word?".formatted(winningPlayer.profile().username()),
                "Win Condition",
                JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.NO_OPTION) {
                // If winner didn't know, they win automatically
                message = "Congratulations, %s! You won because you didn't know the word!".formatted(winningPlayer.profile().username());
            } else {
                // If winner knew, and opponent also guessed their word, it's a tie
                if (state.getPlayerFinishState(losingPlayer) == FinishState.FINISHED_SUCCESS) {
                    soundEffect = SoundEffect.TIE;
                    graphicFile = "tie.png";
                    winner = null; // Mark as tie
                    message = "It's a Tie! Both of you knew your words.";
                } else {
                    message = "Congratulations, %s! You won!".formatted(winningPlayer.profile().username());
                }
            }
        } else { // Someone lost, and didn't guess their word on last chance
            soundEffect = SoundEffect.LOSE;
            graphicFile = "lose.png";
            GamePlayer loser = state.getOpponent(winner); // The one who failed the last guess
            String targetWord = state.wordFor(loser).word();
            message = "Game Over for %s! The word was: %s. %s wins!".formatted(
                loser.profile().username(),
                targetWord,
                winner.profile().username()
            );

            // Offer to shift into solo-mode
            int soloChoice = JOptionPane.showConfirmDialog(
                this,
                "%s, you failed to guess the word. Would you like to continue guessing in solo mode?".formatted(loser.profile().username()),
                "Continue in Solo Mode?",
                JOptionPane.YES_NO_OPTION
            );

            if (soloChoice == JOptionPane.YES_OPTION) {
                // Create new solo game state for the loser to continue guessing the opponent's word
                GameConfig soloConfig = new GameConfig(
                    GameMode.solo,
                    state.getConfig().difficulty(),
                    state.getConfig().wordLength(),   // Correct position for WordLength
                    state.getConfig().timerDuration(), // Correct position for TimerDuration
                    loser,                            // Correct position for playerOne
                    state.getOpponent(loser)          // Correct position for playerTwo
                );
                // The word to guess is the one the loser failed to guess
                WordChoice wordToGuess = state.wordFor(loser);
                
                // This will need to be properly managed by navigation to create a new SoloGamePanel
                // For now, a placeholder to show the intent
                // navigation.showSoloGameWithConfig(soloConfig, wordToGuess); // This method would need to be added to Navigation
                JOptionPane.showMessageDialog(this, "Transitioning to solo mode (feature to be implemented fully).");
                navigation.showLanding(); // For now, go to landing
                return;
            }
        }
        
        // Play sound
        ResourceLoader.playSound(soundEffect);

        // Display graphic
        ImageIcon graphic = ResourceLoader.getImageIcon(graphicFile, 100, 100).orElse(null);
        JOptionPane.showMessageDialog(
            this,
            message,
            (winner != null) ? (winner.profile().username() + " Wins!") : "Game Result",
            JOptionPane.INFORMATION_MESSAGE,
            graphic
        );

        // Optionally navigate back to setup or landing
        navigation.showGameSetup(); // Or showLanding()
    }

    private static final long serialVersionUID = 1L;
}
