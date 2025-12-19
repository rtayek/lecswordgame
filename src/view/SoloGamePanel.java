package view;

import controller.AppController;
import controller.GameOutcomePresenter;
import controller.TurnTimer;
import controller.api.Navigation;
import view.OutcomeRenderer;
import controller.events.GameEvent;
import controller.events.GameEventListener;
import controller.events.GameUiModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.enums.Difficulty;
import model.enums.GameStatus;

import java.awt.Color;
import javax.swing.JOptionPane;

class SoloGamePanel extends JPanel implements TurnTimer.Listener, GameEventListener {

    private final AppController appController;
    private final GameOutcomePresenter outcomePresenter;
    private final TurnTimer timerController;

    SoloGamePanel(Navigation navigation, AppController appController) {
        this.navigation = navigation;
        this.appController = appController;
        this.outcomePresenter = new GameOutcomePresenter();
        this.timerController = navigation.getTimerController();
        
        appController.addGameEventListener(this);
        timerController.addListener(this);

        setLayout(new BorderLayout(8, 8));
        setBackground(new java.awt.Color(0xEEF6F7)); // soft teal tint
        
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
        // This is now just for visibility changes, not state initialization
    }

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.kind()) {
            case gameStarted -> {
                lastModel = event.view();
                grid.clearRows();
                guessField.setText("");
                guessField.setEnabled(true);
                keyboardPanel.setEnabled(true);
                setStatus("New game started. Make your guess!");
                if (lastModel != null) {
                    updateTimerLabel(playerTimerLabel, lastModel.timerDurationSeconds());
                }
            }
            case gameFinished -> onGameFinished(lastModel, null);
            default -> { }
        }
    }
    
    private void handleGuess() {
        try {
            var outcome = appController.submitGuess(guessField.getText());
            var result = outcome.entry().result();
            var difficulty = lastModel != null ? lastModel.difficulty() : model.enums.Difficulty.normal;
            grid.addGuessRow(new GuessRowPanel(result, difficulty));

            if (result.exactMatch()) {
                setStatus("You solved it!");
            } else if (difficulty == Difficulty.expert) {
                setStatus("Correct letters: " + result.correctLetterCount());
            } else {
                setStatus(result.correctLetterCount() + " letters are correct.");
            }

            if (outcome.status() == model.enums.GameStatus.finished) {
                onGameFinished(lastModel, null);
            }
            guessField.setText("");
        } catch (Exception e) {
            setStatus(e.getMessage());
        }
    }
    
    private void onGameFinished(GameUiModel uiModel, Boolean playerKnewWord) {
        // Disable input elements when the game is finished
        guessField.setEnabled(false);
        keyboardPanel.setEnabled(false);

        var vm = outcomePresenter.buildSolo(uiModel, playerKnewWord);
        if (vm == null) {
            return;
        }

        var toShow = vm;
        Boolean finalKnew = playerKnewWord;
        if (vm.nextAction() == GameOutcomePresenter.NextAction.ASK_WINNER_KNOWLEDGE) {
            int choice = JOptionPane.showConfirmDialog(this, vm.message(), vm.title(), JOptionPane.YES_NO_OPTION);
            finalKnew = (choice == JOptionPane.YES_OPTION);
            appController.reportWinnerKnowledge(finalKnew);
            return; // new event will render final outcome
        }

        if (toShow == null) {
            return;
        }

        OutcomeRenderer.render(this, toShow);

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
    public void onTimeUpdated(model.GamePlayer player, int remainingSeconds) {
        if (lastModel == null || player == null || player.profile() == null) return;
        if (!player.profile().username().equals(lastModel.playerOne())) return; // Only update for solo human
        updateTimerLabel(playerTimerLabel, remainingSeconds);
    }

    @Override
    public void onTimeExpired(model.GamePlayer player) {
        if (lastModel == null || player == null || player.profile() == null) return;
        if (!player.profile().username().equals(lastModel.playerOne())) return;
        setStatus(player.profile().username() + " ran out of time!");
    }

    private void updateTimerLabel(JLabel label, int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));

        // Determine if timer should be red
        boolean isTimerRed = false;
        if (lastModel != null && lastModel.timerDurationSeconds() > 0) {
            int gameDuration = lastModel.timerDurationSeconds();
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
    // We track by name/state, not by GamePlayer reference to keep UI decoupled
    private final GuessGridPanel grid;
    private GameUiModel lastModel;
    private final KeyboardPanel keyboardPanel;
    private final JTextField guessField;
    private final JLabel statusLabel;
    private final JLabel playerTimerLabel;
}
