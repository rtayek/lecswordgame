package view;

import controller.AppController;
import controller.GameOutcomePresenter;
import controller.TurnTimer;
import controller.api.GameStateListener;
import controller.api.Navigation;
import view.OutcomeRenderer;
import controller.events.GameEvent;
import controller.events.GameEventListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import model.GameState;
import model.enums.*;
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;

class MultiplayerGamePanel extends JPanel implements TurnTimer.Listener, GameStateListener, GameEventListener {

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

    MultiplayerGamePanel(Navigation navigation, AppController appController) {
        this.navigation = navigation;
        this.appController = appController;
        this.timerController = navigation.getTimerController();
        this.outcomePresenter = new GameOutcomePresenter();
        
        appController.addGameStateListener(this);
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
    public void onGameStateUpdate(GameState newState) {
        updateCurrentPlayerLabel(newState);
    }

    @Override
    public void onGameEvent(GameEvent event) {
        switch (event.kind()) {
            case gameStarted -> {
                leftGrid.clearRows();
                rightGrid.clearRows();
                setStatus(" ");
                guessField.setText("");
                guessField.setEnabled(true);
                keyboardPanel.setEnabled(true);
                submitButton.setEnabled(true);
                var view = event.view();
                if (view != null) {
                    updateTimerLabel(playerOneTimerLabel, view.timerDurationSeconds());
                    updateTimerLabel(playerTwoTimerLabel, view.timerDurationSeconds());
                }
                updateCurrentPlayerLabel(appController.getGameState());
            }
            case gameStateUpdated -> {
                // No-op here; rows are added in handleGuess outcome
                var view = event.view();
                if (view != null && (view.status() == model.enums.GameStatus.waitingForFinalGuess
                        || view.status() == model.enums.GameStatus.awaitingWinnerKnowledge)) {
                    onGameEnd(appController.getGameState(), null);
                } else {
                    updateCurrentPlayerLabel(appController.getGameState());
                }
            }
            case gameFinished -> onGameEnd(appController.getGameState(), null);
            default -> { }
        }
    }

    private void handleGuess() {
        try {
            var outcome = appController.submitGuess(guessField.getText());
            var result = outcome.entry().result();
            var difficulty = appController.getGameState().getConfig().difficulty();

            if (outcome.entry().player().equals(appController.getGameState().getConfig().playerOne())) {
                leftGrid.addGuessRow(new GuessRowPanel(result, difficulty));
            } else {
                rightGrid.addGuessRow(new GuessRowPanel(result, difficulty));
            }

            if (outcome.status() == model.enums.GameStatus.waitingForFinalGuess
                    || outcome.status() == model.enums.GameStatus.finished
                    || outcome.status() == model.enums.GameStatus.awaitingWinnerKnowledge) {
                onGameEnd(appController.getGameState(), null);
            } else {
                updateCurrentPlayerLabel(appController.getGameState());
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

    private void updateCurrentPlayerLabel(GameState state) {
        if (state == null) {
            currentPlayerLabel.setText("Current player: (none)");
            submitButton.setEnabled(false);
            guessField.setEnabled(false);
            keyboardPanel.setEnabled(false);
            return;
        }

        if (state.getStatus() == GameStatus.finished) {
            var winner = state.getWinner();
            if (winner == null) {
                currentPlayerLabel.setText("Game finished: It's a Tie!");
            } else {
                var name = winner.profile() != null ? winner.profile().username() : (winner.equals(state.getConfig().playerOne()) ? "Player 1" : "Player 2");
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
            guessField.setEnabled(false);
            keyboardPanel.setEnabled(false);
            return;
        }

        var name = player.profile().username();
        if (name == null || name.isBlank()) {
            name = player.equals(state.getConfig().playerOne()) ? "Player 1" : "Player 2";
        }

        currentPlayerLabel.setText("Current player: " + name);
        submitButton.setEnabled(true);
        guessField.setEnabled(true);
        keyboardPanel.setEnabled(true);
    }

    void onShow() {
        // This method is now only for visibility changes, not state initialization.
        var state = appController.getGameState();
        updateCurrentPlayerLabel(state);
        if (state != null) {
            updateTimerLabel(playerOneTimerLabel, timerController.getRemainingFor(state.getConfig().playerOne()));
            updateTimerLabel(playerTwoTimerLabel, timerController.getRemainingFor(state.getConfig().playerTwo()));
        }
    }

    @Override
    public void onTimeUpdated(model.GamePlayer player, int remainingSeconds) {
        var state = appController.getGameState();
        if (state == null) return;
        
        JLabel labelToUpdate = (player.equals(state.getConfig().playerOne())) ? playerOneTimerLabel : playerTwoTimerLabel;
        updateTimerLabel(labelToUpdate, remainingSeconds);
    }

    @Override
    public void onTimeExpired(model.GamePlayer player) {
        var state = appController.getGameState();
        if (state == null) return;

        String name = player.profile().username();
        if (name == null || name.isBlank()) {
            name = player.equals(state.getConfig().playerOne()) ? "Player 1" : "Player 2";
        }
        setStatus(name + " ran out of time!");
    }
    
    private void updateTimerLabel(JLabel label, int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        label.setText(String.format("%02d:%02d", minutes, seconds));

        boolean isTimerRed = false;
        var gameState = appController.getGameState();
        if (gameState != null && gameState.getConfig().timerDuration().isTimed()) {
            int gameDuration = gameState.getConfig().timerDuration().seconds();
            if (gameDuration >= 3 * 60 && totalSeconds < 60) {
                isTimerRed = true;
            } else if (gameDuration == 1 * 60 && totalSeconds < 30) {
                isTimerRed = true;
            }
        }
        label.setForeground(isTimerRed ? Color.RED : Color.BLACK);
    }
    
    private void onGameEnd(GameState state, Boolean winnerKnewWord) {
        var vm = outcomePresenter.buildMultiplayer(state, winnerKnewWord);
        if (vm == null) {
            return;
        }

        if (vm.nextAction() == GameOutcomePresenter.NextAction.SHOW_LAST_CHANCE) {
            JOptionPane.showMessageDialog(this, vm.message(), vm.title(), JOptionPane.INFORMATION_MESSAGE);
            submitButton.setEnabled(true);
            guessField.setEnabled(true);
            keyboardPanel.setEnabled(true);
            updateCurrentPlayerLabel(state);
            return;
        }

        var toShow = vm;
        Boolean finalWinnerKnew = winnerKnewWord;
        if (vm.nextAction() == GameOutcomePresenter.NextAction.ASK_WINNER_KNOWLEDGE) {
            int choice = JOptionPane.showConfirmDialog(this, vm.message(), vm.title(), JOptionPane.YES_NO_OPTION);
            finalWinnerKnew = (choice == JOptionPane.YES_OPTION);
            appController.reportWinnerKnowledge(finalWinnerKnew);
            toShow = outcomePresenter.buildMultiplayer(appController.getGameState(), finalWinnerKnew);
        }

        if (toShow == null) {
            return;
        }

        OutcomeRenderer.render(this, toShow);
        navigation.showGameSetup();
    }

    private static final long serialVersionUID = 1L;
}
