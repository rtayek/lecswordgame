package view;

import controller.AppController;
import controller.TimerController;
import view.listeners.GameEventListener;
import view.listeners.GameStateListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.Enums.Difficulty;
import model.Records.GamePlayer;
import model.Records.PlayerProfile;
import model.GameState;

import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
// import java.net.URL; // Removed: ResourceLoader handles URL
// import java.awt.Image; // Removed: no longer needed for manual scaling
// import java.awt.image.BufferedImage; // Removed: no longer needed for manual scaling
// import java.awt.Graphics2D; // Removed: no longer needed for manual scaling
// import java.awt.RenderingHints; // Removed: no longer needed for manual scaling
import model.Enums.GameStatus;
import util.ResourceLoader; // Import ResourceLoader
import util.SoundEffect;

class SoloGamePanel extends JPanel implements TimerController.Listener, GameStateListener, GameEventListener {

    private final AppController appController;
    private final TimerController timerController;

    SoloGamePanel(Navigation navigation, AppController appController, TimerController timerController) {
        this.navigation = navigation;
        this.appController = appController;
        this.timerController = timerController;
        this.player = new GamePlayer(new PlayerProfile("You", ""), true);
        
        appController.addGameStateListener(this);
        appController.addGameEventListener(this);

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
        timerController.setListener(this);
        // This is now just for visibility changes, not state initialization
    }

    @Override
    public void onGameStart(GameState initialState) {
        grid.clearRows();
        guessField.setText("");
        guessField.setEnabled(true);
        keyboardPanel.setEnabled(true);
        setStatus("New game started. Make your guess!");
        updateTimerLabel(playerTimerLabel, initialState.getConfig().timerDuration().seconds());
    }

    @Override
    public void onGameStateUpdate(GameState newState) {
        if (newState.getGuesses().isEmpty()) {
            return;
        }
        var latestGuess = newState.getGuesses().get(newState.getGuesses().size() - 1);
        var result = latestGuess.result();
        var difficulty = newState.getConfig().difficulty();
        
        grid.addGuessRow(new GuessRowPanel(result, difficulty));

        if (result.exactMatch()) {
            setStatus("You solved it!");
        } else if (difficulty == Difficulty.expert) {
            setStatus("Correct letters: " + result.correctLetterCount());
        } else {
            setStatus(result.correctLetterCount() + " letters are correct.");
        }
    }

    @Override
    public void onGameOver(GameState finalState) {
        onGameFinished(finalState);
    }
    
    private void handleGuess() {
        try {
            appController.submitGuess(guessField.getText());
            guessField.setText("");
        } catch (Exception e) {
            setStatus(e.getMessage());
        }
    }
    
    private void onGameFinished(GameState state) {
        // Disable input elements when the game is finished
        guessField.setEnabled(false);
        keyboardPanel.setEnabled(false);

        boolean playerWon = (state.getWinner() != null && state.getWinner().equals(player));
        String message;
        SoundEffect soundEffect;
        String graphicFile; // Placeholder for graphic file path

        if (playerWon) {
            soundEffect = SoundEffect.WIN; // Use SoundEffect enum
            graphicFile = "win.png"; // Use filename directly (corrected to .png)
            
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
            soundEffect = SoundEffect.LOSE; // Use SoundEffect enum
            graphicFile = "lose.png"; // Use filename directly (corrected to .png)
            String targetWord = state.wordFor(player).word(); // Get the word the player was guessing
            message = "Game Over! The word was: " + targetWord + ". You lost!";
        }
        
        // Play sound
        ResourceLoader.playSound(soundEffect); // Use ResourceLoader
        
        // Handle Optional<ImageIcon>
        ImageIcon graphicIcon = ResourceLoader.getImageIcon(graphicFile, 100, 100).orElse(null);

        // Display graphic (for now, a simple message dialog)
        JOptionPane.showMessageDialog(
            this,
            message,
            playerWon ? "You Win!" : "You Lose!",
            JOptionPane.INFORMATION_MESSAGE,
            graphicIcon
        );

        // Optionally navigate back to setup or landing
        navigation.showGameSetup(); // Or showLanding()
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
        var state = appController.getGameState();
        if (state == null || player == null || !player.equals(this.player)) return; // Only update for this player

        updateTimerLabel(playerTimerLabel, remainingSeconds);
    }

    @Override
    public void onTimeExpired(GamePlayer player) {
        var state = appController.getGameState();
        if (state == null || player == null || !player.equals(this.player)) return; // Only update for this player
        
        setStatus(player.profile().username() + " ran out of time!");
    }

    private void updateTimerLabel(JLabel label, int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));

        // Determine if timer should be red
        boolean isTimerRed = false;
        var gameState = appController.getGameState();
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
    private final GamePlayer player; // Assuming this is the solo player
    private final GuessGridPanel grid;
    private final KeyboardPanel keyboardPanel;
    private final JTextField guessField;
    private final JLabel statusLabel;
    private final JLabel playerTimerLabel;
}
