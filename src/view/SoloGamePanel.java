package view;

import controller.GameController;
import controller.TimerController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.Enums.Difficulty;
import model.Records.GamePlayer;
import model.Records.GuessResult;
import model.Records.PlayerProfile;
import model.GameState; // Missing Import

import java.awt.Color; // Missing Import
import javax.swing.JOptionPane; // Missing Import
import javax.swing.ImageIcon; // Missing Import
import java.net.URL; // Missing Import
import java.awt.Image; // Missing Import
import java.awt.image.BufferedImage; // Missing Import
import java.awt.Graphics2D; // Missing Import
import java.awt.RenderingHints; // Missing Import
import model.Enums.GameStatus; // Missing Import

class SoloGamePanel extends JPanel implements TimerController.Listener {

    SoloGamePanel(Navigation navigation, GameController gameController) {
        this.navigation = navigation;
        this.gameController = gameController;
        this.player = new GamePlayer(new PlayerProfile("You", ""), true);
        navigation.getTimerController().setListener(this);

        setLayout(new BorderLayout(8, 8));
        
        var topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Solo Game"));
        playerTimerLabel = new JLabel("00:00");
        topPanel.add(playerTimerLabel);
        add(topPanel, BorderLayout.NORTH);
        
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
        var state = navigation.getGameState();
        if (state != null) {
            updateTimerLabel(playerTimerLabel, navigation.getTimerController().getRemainingFor(player));
        } else {
            updateTimerLabel(playerTimerLabel, 0);
        }
    }
    
    private void handleGuess() {
        GuessUIHelper.Outcome outcome = GuessUIHelper.submitGuess(
                navigation,
                gameController,
                gs -> player,
                guessField.getText(),
                "Please start a new game from the setup screen."
        );

        if (outcome.isError()) {
            setStatus(outcome.errorMessage());
            return;
        }

        var result = outcome.result();
        var difficulty = outcome.state().getConfig().difficulty();
        grid.addGuessRow(new GuessRowPanel(result, difficulty));
        guessField.setText("");

        if (result.exactMatch()) {
            setStatus("You solved it!");
        } else if (difficulty == Difficulty.expert) {
            setStatus("Correct letters: " + result.correctLetterCount());
        } else {
            setStatus(result.correctLetterCount() + " letters are correct.");
        }

        if (outcome.state().getStatus() == GameStatus.finished) {
            onGameFinished(outcome.state());
        }
    }
    
    private void onGameFinished(GameState state) {
        boolean playerWon = (state.getWinner() != null && state.getWinner().equals(player));
        String message;
        String soundFile;
        String graphicFile; // Placeholder for graphic file path

        if (playerWon) {
            soundFile = "/main/resources/win.wav";
            graphicFile = "/main/resources/win_graphic.png";
            
            // "Did you know this word?" prompt
            int choice = JOptionPane.showConfirmDialog(
                this,
                "You guessed the word! Did you know this word?",
                "Win Condition",
                JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.NO_OPTION) {
                // If they did not know the word, they automatically win.
                message = "Congratulations! You won because you didn't know the word!";
            } else {
                // If they knew the word, it's just a regular win.
                message = "Congratulations! You won!";
            }
        } else {
            soundFile = "/main/resources/lose.wav";
            graphicFile = "/main/resources/lose_graphic.png";
            String targetWord = state.wordFor(player).word(); // Get the word the player was guessing
            message = "Game Over! The word was: " + targetWord + ". You lost!";
        }
        
        // Play sound
        util.AudioPlayer.playSound(getClass().getResource(soundFile).getPath());

        // Display graphic (for now, a simple message dialog)
        JOptionPane.showMessageDialog(
            this,
            message,
            playerWon ? "You Win!" : "You Lose!",
            JOptionPane.INFORMATION_MESSAGE,
            getGraphicIcon(graphicFile)
        );

        // Optionally navigate back to setup or landing
        navigation.showGameSetup(); // Or showLanding()
    }

    // Helper to get a scaled graphic icon
    private ImageIcon getGraphicIcon(String path) {
        URL imageUrl = getClass().getResource(path);
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Scale for dialog
            return new ImageIcon(scaledImage);
        }
        return null; // No graphic
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

    @Override
    public void onTimeUpdated(GamePlayer player, int remainingSeconds) {
        var state = navigation.getGameState();
        if (state == null || player == null || !player.equals(this.player)) return; // Only update for this player

        updateTimerLabel(playerTimerLabel, remainingSeconds);
    }

    @Override
    public void onTimeExpired(GamePlayer player) {
        var state = navigation.getGameState();
        if (state == null || player == null || !player.equals(this.player)) return; // Only update for this player
        
        setStatus(player.profile().username() + " ran out of time!");
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

    private static final long serialVersionUID = 1L;

    private final Navigation navigation;
    private final GameController gameController;
    private final GamePlayer player; // Assuming this is the solo player
    private final GuessGridPanel grid;
    private final KeyboardPanel keyboardPanel;
    private final JTextField guessField;
    private final JLabel statusLabel;
    private final JLabel playerTimerLabel;
}
