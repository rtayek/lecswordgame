package view;

import controller.AppController;
import controller.GameOutcomePresenter;
import controller.TurnTimer;
import controller.api.Navigation;
import controller.events.GameEvent;
import controller.events.GameEventListener;
import controller.events.GameUiModel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import model.enums.Difficulty;

abstract class BaseGamePanel extends JPanel implements TurnTimer.Listener, GameEventListener {

    protected final AppController appController;
    protected final Navigation navigation;
    protected final TurnTimer timerController;
    protected final GameOutcomePresenter outcomePresenter;
    protected final JTextField guessField;
    protected final KeyboardPanel keyboardPanel;
    protected final JLabel statusLabel;
    protected final JButton submitButton;
    protected GameUiModel lastModel;

    BaseGamePanel(Navigation navigation, AppController appController) {
        this.navigation = navigation;
        this.appController = appController;
        this.timerController = navigation.getTimerController();
        this.outcomePresenter = new GameOutcomePresenter();

        appController.addGameEventListener(this);
        timerController.addListener(this);

        setLayout(new BorderLayout(8, 8));

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

        submitButton = new JButton("Submit Guess");
        submitButton.addActionListener(e -> handleGuess());
    }

    protected void handleGuess() {
        try {
            var outcome = appController.submitGuess(guessField.getText());
            var result = outcome.entry().result();
            var difficulty = lastModel != null ? lastModel.difficulty() : "normal";

            addGuessRow(outcome.entry().player(), result, mapDifficulty(difficulty));

            var statusName = outcome.status().name();
            if ("waitingForFinalGuess".equalsIgnoreCase(statusName)
                    || "finished".equalsIgnoreCase(statusName)
                    || "awaitingWinnerKnowledge".equalsIgnoreCase(statusName)) {
                onGameFinished(lastModel, null);
            } else {
                updateCurrentPlayerLabelFromModel();
            }
            guessField.setText("");
        } catch (Exception e) {
            setStatus(e.getMessage());
        }
    }

    protected void handleBackspace() {
        var text = guessField.getText();
        if (text != null && !text.isEmpty()) {
            guessField.setText(text.substring(0, text.length() - 1));
        }
    }

    protected void setStatus(String text) {
        statusLabel.setText(text);
    }

    protected void updateTimerLabel(JLabel label, int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));

        boolean isTimerRed = false;
        if (lastModel != null && lastModel.timerDurationSeconds() > 0) {
            int gameDuration = lastModel.timerDurationSeconds();
            if (gameDuration >= 3 * 60 && totalSeconds < 60) {
                isTimerRed = true;
            } else if (gameDuration == 1 * 60 && totalSeconds < 30) {
                isTimerRed = true;
            }
        }
        label.setForeground(isTimerRed ? Color.RED : Color.BLACK);
    }
    
    protected Difficulty mapDifficulty(String value) {
        try {
            return Difficulty.valueOf(value);
        } catch (Exception e) {
            return Difficulty.normal;
        }
    }
    
    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.kind()) {
            case gameStarted -> onGameStarted(event.view());
            case gameStateUpdated -> onGameStateUpdated(event.view());
            case gameFinished -> onGameFinished(event.view(), null);
            default -> {
            }
        }
    }
    
    protected void onGameStarted(GameUiModel model) {
        lastModel = model;
        setStatus(" ");
        guessField.setText("");
        guessField.setEnabled(true);
        keyboardPanel.setEnabled(true);
        submitButton.setEnabled(true);
        updateCurrentPlayerLabelFromModel();
    }

    protected void onGameStateUpdated(GameUiModel model) {
        lastModel = model;
        if (lastModel != null && ("waitingForFinalGuess".equalsIgnoreCase(lastModel.status())
                || "awaitingWinnerKnowledge".equalsIgnoreCase(lastModel.status()))) {
            onGameFinished(lastModel, null);
        } else {
            updateCurrentPlayerLabelFromModel();
        }
    }

    abstract void onGameFinished(GameUiModel uiModel, Boolean winnerKnewWord);
    
    abstract void addGuessRow(model.GamePlayer player, model.GuessResult result, Difficulty difficulty);

    abstract void updateCurrentPlayerLabelFromModel();
}
