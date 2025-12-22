package view;

import controller.AppController;
import controller.GameOutcomePresenter;
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
import java.awt.Color;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import controller.NextAction;
import controller.OutcomeViewModel;

class MultiplayerGamePanel extends BaseGamePanel {

    private final GuessGridPanel leftGrid;
    private final GuessGridPanel rightGrid;
    private final JLabel currentPlayerLabel;
    private final JLabel playerOneLabel;
    private final JLabel playerTwoLabel;
    private final JLabel playerOneTimerLabel;
    private final JLabel playerTwoTimerLabel;

    MultiplayerGamePanel(Navigation navigation, AppController appController) {
        super(navigation, appController);

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

        var controls = new JPanel();
        controls.add(submitButton);

        var backspace = new JButton("Backspace");
        backspace.addActionListener(e -> handleBackspace());
        
        var enter = new JButton("Enter");
        enter.addActionListener(e -> handleGuess());

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());

        controls.add(backspace);
        controls.add(enter);
        controls.add(back);
        add(controls, BorderLayout.WEST);
    }
    
    @Override
    protected void onGameStarted(GameUiModel model) {
        super.onGameStarted(model);
        leftGrid.clearRows();
        rightGrid.clearRows();
        if (lastModel != null) {
            updateTimerLabel(playerOneTimerLabel, lastModel.timerDurationSeconds());
            updateTimerLabel(playerTwoTimerLabel, lastModel.timerDurationSeconds());
        }
    }

    @Override
    void onGameFinished(GameUiModel uiModel) {
        var vm = outcomePresenter.build(uiModel);
        if (vm == null) {
            return;
        }

        if (vm.nextAction() == NextAction.SHOW_LAST_CHANCE) {
            JOptionPane.showMessageDialog(this, vm.message(), vm.title(), JOptionPane.INFORMATION_MESSAGE);
            submitButton.setEnabled(true);
            guessField.setEnabled(true);
            keyboardPanel.setEnabled(true);
            updateCurrentPlayerLabelFromModel();
            return;
        }

        var toShow = vm;
        if (vm.nextAction() == NextAction.ASK_WINNER_KNOWLEDGE) {
            int choice = JOptionPane.showConfirmDialog(this, vm.message(), vm.title(), JOptionPane.YES_NO_OPTION);
            boolean finalWinnerKnew = (choice == JOptionPane.YES_OPTION);
            appController.reportWinnerKnowledge(finalWinnerKnew);
            toShow = outcomePresenter.build(uiModel);
        }

        if (toShow == null) {
            return;
        }

        OutcomeRenderer.render(this, toShow);
        navigation.showGameSetup();
    }

    @Override
    void addGuessRow(controller.events.GuessView guessView, controller.events.DifficultyView difficulty) {
        var result = guessView.result();
        if (guessView.isPlayerOne()) {
            leftGrid.addGuessRow(new GuessRowPanel(result, difficulty));
        } else {
            rightGrid.addGuessRow(new GuessRowPanel(result, difficulty));
        }
    }
    
    @Override
    void updateCurrentPlayerLabelFromModel() {
        if (lastModel == null) {
            currentPlayerLabel.setText("Current player: (none)");
            submitButton.setEnabled(false);
            guessField.setEnabled(false);
            keyboardPanel.setEnabled(false);
            return;
        }

        if (lastModel.status() == controller.events.GameStatusView.finished) {
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

    @Override
    void updateTimersFromModel(GameUiModel model) {
        if (model == null) return;
        Integer p1 = model.playerOneRemaining();
        Integer p2 = model.playerTwoRemaining();
        int defaultSeconds = model.timerDurationSeconds();
        updateTimerLabel(playerOneTimerLabel, p1 != null ? p1 : defaultSeconds);
        updateTimerLabel(playerTwoTimerLabel, p2 != null ? p2 : defaultSeconds);
        if (p1 != null && p1 <= 0) {
            String name = model.playerOne() == null ? "Player" : model.playerOne();
            setStatus(name + " ran out of time!");
        } else if (p2 != null && p2 <= 0) {
            String name = model.playerTwo() == null ? "Player" : model.playerTwo();
            setStatus(name + " ran out of time!");
        }
    }

    @Override
    void updateTimersFromTimer(controller.events.TimerView timerView) {
        if (timerView == null) return;
        int remaining = timerView.remainingSeconds();
        switch (timerView.slot()) {
            case playerOne -> {
                updateTimerLabel(playerOneTimerLabel, remaining);
                if (remaining <= 0 && lastModel != null) {
                    String name = lastModel.playerOne() == null ? "Player" : lastModel.playerOne();
                    setStatus(name + " ran out of time!");
                }
            }
            case playerTwo -> {
                updateTimerLabel(playerTwoTimerLabel, remaining);
                if (remaining <= 0 && lastModel != null) {
                    String name = lastModel.playerTwo() == null ? "Player" : lastModel.playerTwo();
                    setStatus(name + " ran out of time!");
                }
            }
            default -> { }
        }
    }
}
