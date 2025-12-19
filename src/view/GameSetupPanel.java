package view;

import controller.AppController;
import controller.api.Navigation;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.enums.*;

class GameSetupPanel extends JPanel {

    private JComboBox<WordLength> wordLengthComboBox;
    private JComboBox<Difficulty> difficultyComboBox;
    private JComboBox<TimerDuration> timerDurationComboBox;

    GameSetupPanel(Navigation navigation, AppController appController) {
        setLayout(new BorderLayout(8, 8));
        setBackground(new java.awt.Color(0xE3F2FD)); // light blue
        add(new JLabel("Game Setup"), BorderLayout.NORTH);

        var optionsPanel = new JPanel(new GridLayout(0, 1, 6, 6)); // Main panel for all options
        optionsPanel.setOpaque(false);

        // Word Length Selection
        JPanel wordLengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wordLengthPanel.add(new JLabel("Word Length:"));
        wordLengthComboBox = new JComboBox<>(WordLength.values());
        wordLengthComboBox.setSelectedItem(WordLength.five); // Default
        wordLengthPanel.add(wordLengthComboBox);
        optionsPanel.add(wordLengthPanel);

        // Difficulty Selection
        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        difficultyPanel.add(new JLabel("Difficulty:"));
        difficultyComboBox = new JComboBox<>(Difficulty.values());
        difficultyComboBox.setSelectedItem(Difficulty.normal); // Default
        difficultyPanel.add(difficultyComboBox);
        optionsPanel.add(difficultyPanel);

        // Timer Duration Selection
        JPanel timerDurationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerDurationPanel.add(new JLabel("Timer:"));
        timerDurationComboBox = new JComboBox<>(TimerDuration.values());
        timerDurationComboBox.setSelectedItem(TimerDuration.none); // Default
        timerDurationPanel.add(timerDurationComboBox);
        optionsPanel.add(timerDurationPanel);


        var gameButtonsPanel = new JPanel(new GridLayout(1, 2, 8, 8)); // Panel for Start buttons

        var multiplayer = new JButton("Start Multiplayer");
        multiplayer.addActionListener(e -> {
            appController.startMultiplayerGame("Player 1", "Player 2",
                    (Difficulty) difficultyComboBox.getSelectedItem(),
                    (WordLength) wordLengthComboBox.getSelectedItem(),
                    (TimerDuration) timerDurationComboBox.getSelectedItem());
        });

        var solo = new JButton("Start Solo");
        solo.addActionListener(e -> {
            appController.startSoloGame("You",
                    (Difficulty) difficultyComboBox.getSelectedItem(),
                    (WordLength) wordLengthComboBox.getSelectedItem(),
                    (TimerDuration) timerDurationComboBox.getSelectedItem());
        });

        gameButtonsPanel.add(multiplayer);
        gameButtonsPanel.add(solo);
        optionsPanel.add(gameButtonsPanel); // Add game buttons to the main options panel

        add(optionsPanel, BorderLayout.CENTER); // Add the combined options and game buttons panel to center

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }
    
    static final long serialVersionUID = 1L;
}
