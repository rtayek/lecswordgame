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

import java.awt.Color;
import javax.swing.JOptionPane;

class SoloGamePanel extends BaseGamePanel {

    private final GuessGridPanel grid;
    private final JLabel playerTimerLabel;

    SoloGamePanel(Navigation navigation, AppController appController) {
        super(navigation, appController);

        setBackground(new java.awt.Color(0xEEF6F7)); // soft teal tint

        var topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Solo Game"));
        playerTimerLabel = new JLabel("00:00");
        topPanel.add(playerTimerLabel);
        add(topPanel, BorderLayout.NORTH);

        grid = new GuessGridPanel();
        add(new JScrollPane(grid), BorderLayout.CENTER);

        var controls = new JPanel();
        controls.add(submitButton);

        var backspace = new JButton("Backspace");
        backspace.addActionListener(e -> handleBackspace());

        var back = new JButton("Back to Setup");
        back.addActionListener(e -> navigation.showGameSetup());

        controls.add(backspace);
        controls.add(back);
        add(controls, BorderLayout.WEST);
    }

    @Override
    protected void onGameStarted(GameUiModel model) {
        super.onGameStarted(model);
        grid.clearRows();
        setStatus("New game started. Make your guess!");
        if (lastModel != null) {
            updateTimerLabel(playerTimerLabel, lastModel.timerDurationSeconds());
        }
    }

    @Override
    void onGameFinished(GameUiModel uiModel, Boolean playerKnewWord) {
        // Disable input elements when the game is finished
        guessField.setEnabled(false);
        keyboardPanel.setEnabled(false);

        var vm = outcomePresenter.build(uiModel, playerKnewWord);
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

    @Override
    void addGuessRow(model.GamePlayer player, model.GuessResult result, Difficulty difficulty) {
        grid.addGuessRow(new GuessRowPanel(result, difficulty));
    }

    @Override
    void updateCurrentPlayerLabelFromModel() {
        // No-op for solo game
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
}

enum DifficultyView {
    normal, hard, expert
}
