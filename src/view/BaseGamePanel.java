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
            appController.submitGuess(guessField.getText());
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
    
    protected controller.events.DifficultyView mapDifficulty(controller.events.DifficultyView value) {
        return value == null ? controller.events.DifficultyView.normal : value;
    }
    
    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.kind()) {
            case gameStarted -> onGameStarted(event.view());
            case gameStateUpdated -> onGameStateUpdated(event.view());
            case gameFinished -> onGameFinished(event.view());
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
        renderGuesses(model, 0);
        updateCurrentPlayerLabelFromModel();
        keyboardPanel.apply(model.keyboard(), mapDifficulty(model.difficulty()));
    }

    protected void onGameStateUpdated(GameUiModel model) {
        int previousGuessCount = lastModel != null && lastModel.guesses() != null ? lastModel.guesses().size() : 0;
        lastModel = model;
        renderGuesses(model, previousGuessCount);
        updateCurrentPlayerLabelFromModel();
        keyboardPanel.apply(model.keyboard(), mapDifficulty(model.difficulty()));
    }

    private void renderGuesses(GameUiModel model, int alreadyRendered) {
        var guesses = model.guesses();
        if (guesses == null) return;
        var diff = mapDifficulty(model.difficulty());
        for (int i = alreadyRendered; i < guesses.size(); i++) {
            addGuessRow(guesses.get(i), diff);
        }
    }

    abstract void onGameFinished(GameUiModel uiModel);
    
    abstract void addGuessRow(controller.events.GuessView guessView, controller.events.DifficultyView difficulty);

    abstract void updateCurrentPlayerLabelFromModel();
}
