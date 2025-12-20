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
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.enums.*;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

class MultiplayerGamePanel extends JPanel implements TurnTimer.Listener, GameEventListener {

    private final AppController appController;
    private final Navigation navigation;
    private final TurnTimer timerController;
    private final GameOutcomePresenter outcomePresenter;
    private final GuessGridPanel leftGrid;
    private final GuessGridPanel rightGrid;
    private final JLabel currentPlayerLabel;
    private final JLabel playerOneLabel;
    private final JLabel playerTwoLabel;
    private final JLabel playerOneTimerLabel;
    private final JLabel playerTwoTimerLabel;
    private final JTextField guessField;
    private final KeyboardPanel keyboardPanel;
    private final JLabel statusLabel;
    private final JButton submitButton;
    private GameUiModel lastModel;

    MultiplayerGamePanel(Navigation navigation, AppController appController) {
        this.navigation = navigation;
        this.appController = appController;
        this.timerController = navigation.getTimerController();
        this.outcomePresenter = new GameOutcomePresenter();
        
        appController.addGameEventListener(this);
        timerController.addListener(this);

        setLayout(new BorderLayout(8, 8));
        setBackground(new java.awt.Color(0xF4F1DE)); // warm beige

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

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.kind()) {
            case gameStarted -> {
                lastModel = event.view();
                leftGrid.clearRows();
                rightGrid.clearRows();
                setStatus(" ");
                guessField.setText("");
                guessField.setEnabled(true);
                keyboardPanel.setEnabled(true);
                submitButton.setEnabled(true);
                if (lastModel != null) {
                    updateTimerLabel(playerOneTimerLabel, lastModel.timerDurationSeconds());
                    updateTimerLabel(playerTwoTimerLabel, lastModel.timerDurationSeconds());
                }
                updateCurrentPlayerLabelFromModel();
            }
            case gameStateUpdated -> {
                // No-op here; rows are added in handleGuess outcome
                lastModel = event.view();
                if (lastModel != null && (lastModel.status() == model.enums.GameStatus.waitingForFinalGuess
                        || lastModel.status() == model.enums.GameStatus.awaitingWinnerKnowledge)) {
                    onGameEnd(lastModel, null);
                } else {
                    updateCurrentPlayerLabelFromModel();
                }
            }
            case gameFinished -> {
                lastModel = event.view();
                onGameEnd(lastModel, null);
            }
            default -> { }
        }
    }

    private void handleGuess() {
        try {
            var outcome = appController.submitGuess(guessField.getText());
            var result = outcome.entry().result();
            var difficulty = lastModel != null ? lastModel.difficulty() : "normal";

            // Determine target grid by player name
            boolean isPlayerOne = lastModel != null && lastModel.playerOne() != null
                    && outcome.entry().player() != null
                    && outcome.entry().player().profile() != null
                    && lastModel.playerOne().equals(outcome.entry().player().profile().username());
            if (isPlayerOne) {
                leftGrid.addGuessRow(new GuessRowPanel(result, mapDifficulty(difficulty)));
            } else {
                rightGrid.addGuessRow(new GuessRowPanel(result, mapDifficulty(difficulty)));
            }

            if (outcome.status() == model.enums.GameStatus.waitingForFinalGuess
                    || outcome.status() == model.enums.GameStatus.finished
                    || outcome.status() == model.enums.GameStatus.awaitingWinnerKnowledge) {
                onGameEnd(lastModel, null);
            } else {
                updateCurrentPlayerLabelFromModel();
            }
            guessField.setText("");
        } catch (Exception e) {
            setStatus(e.getMessage());
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

    private void updateCurrentPlayerLabelFromModel() {
        if (lastModel == null) {
            currentPlayerLabel.setText("Current player: (none)");
            submitButton.setEnabled(false);
            guessField.setEnabled(false);
            keyboardPanel.setEnabled(false);
            return;
        }

        if (lastModel.status() == GameStatus.finished) {
            var winnerName = lastModel.winner();
            if (winnerName == null) {
                currentPlayerLabel.setText("Game finished: It's a Tie!");
            } else {
                currentPlayerLabel.setText(winnerName + " wins!");
            }
            submitButton.setEnabled(false);
            guessField.setEnabled(false);
            keyboardPanel.setEnabled(false);
            return;
        }

        var name = lastModel.currentPlayer();
        if (name == null || name.isBlank()) {
            name = "Player";
        }

        currentPlayerLabel.setText("Current player: " + name);
        submitButton.setEnabled(true);
        guessField.setEnabled(true);
        keyboardPanel.setEnabled(true);
    }

    void onShow() {
        // This method is now only for visibility changes, not state initialization.
        updateCurrentPlayerLabelFromModel();
        if (lastModel != null) {
            // We cannot fetch per-player remaining without state; display configured duration.
            updateTimerLabel(playerOneTimerLabel, lastModel.timerDurationSeconds());
            updateTimerLabel(playerTwoTimerLabel, lastModel.timerDurationSeconds());
        }
    }

    @Override
    public void onTimeUpdated(model.GamePlayer player, int remainingSeconds) {
        if (lastModel == null || player == null || player.profile() == null) return;
        if (lastModel.playerOne() != null && lastModel.playerOne().equals(player.profile().username())) {
            updateTimerLabel(playerOneTimerLabel, remainingSeconds);
        } else if (lastModel.playerTwo() != null && lastModel.playerTwo().equals(player.profile().username())) {
            updateTimerLabel(playerTwoTimerLabel, remainingSeconds);
        }
    }

    @Override
    public void onTimeExpired(model.GamePlayer player) {
        if (player == null || player.profile() == null) return;
        String name = player.profile().username();
        setStatus((name == null || name.isBlank() ? "Player" : name) + " ran out of time!");
    }
    
    private void updateTimerLabel(JLabel label, int totalSeconds) {
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
    
    private void onGameEnd(GameUiModel uiModel, Boolean winnerKnewWord) {
        var vm = outcomePresenter.buildMultiplayer(uiModel, winnerKnewWord);
        if (vm == null) {
            return;
        }

        if (vm.nextAction() == GameOutcomePresenter.NextAction.SHOW_LAST_CHANCE) {
            JOptionPane.showMessageDialog(this, vm.message(), vm.title(), JOptionPane.INFORMATION_MESSAGE);
            submitButton.setEnabled(true);
            guessField.setEnabled(true);
            keyboardPanel.setEnabled(true);
            updateCurrentPlayerLabelFromModel();
            return;
        }

        var toShow = vm;
        Boolean finalWinnerKnew = winnerKnewWord;
        if (vm.nextAction() == GameOutcomePresenter.NextAction.ASK_WINNER_KNOWLEDGE) {
            int choice = JOptionPane.showConfirmDialog(this, vm.message(), vm.title(), JOptionPane.YES_NO_OPTION);
            finalWinnerKnew = (choice == JOptionPane.YES_OPTION);
            appController.reportWinnerKnowledge(finalWinnerKnew);
            toShow = outcomePresenter.buildMultiplayer(uiModel, finalWinnerKnew);
        }

        if (toShow == null) {
            return;
        }

        OutcomeRenderer.render(this, toShow);
        navigation.showGameSetup();
    }

    private Difficulty mapDifficulty(String value) {
        try {
            return Difficulty.valueOf(value);
        } catch (Exception e) {
            return Difficulty.normal;
        }
    }

    private static final long serialVersionUID = 1L;
}
