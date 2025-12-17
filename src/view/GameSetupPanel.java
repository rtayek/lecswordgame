package view;

import controller.GameController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Enums.*;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.PlayerProfile;
import model.Records.WordChoice;

class GameSetupPanel extends JPanel {

    private JComboBox<WordLength> wordLengthComboBox;
    private JComboBox<Difficulty> difficultyComboBox;
    private JComboBox<TimerDuration> timerDurationComboBox;

    GameSetupPanel(Navigation navigation, GameController gameController) {
        this.navigation = navigation;
        this.gameController = gameController;
        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Game Setup"), BorderLayout.NORTH);

        var optionsPanel = new JPanel(new GridLayout(0, 1, 6, 6)); // Main panel for all options

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
            var playerOne = new GamePlayer(new PlayerProfile("Player 1", ""), true); // Placeholder profiles
            var playerTwo = new GamePlayer(new PlayerProfile("Player 2", ""), true);

            var config = createConfig(GameMode.multiplayer, playerOne, playerTwo);
            navigation.showWordSelection(config, playerOne, playerTwo, true);
        });

        var solo = new JButton("Start Solo");
        solo.addActionListener(e -> {
            var player = new GamePlayer(new PlayerProfile("You", ""), true); // Human player
            var computerPlayer = new GamePlayer(new PlayerProfile("Computer", ""), false); // Computer player
            var config = createConfig(GameMode.solo, player, computerPlayer);
            // In solo mode, the human player guesses the computer's word.
            // So playerOne (human) is implicitly guessing playerTwo's (computer's) word.
            // We ask playerTwo (computer) to "select" its word first.
            navigation.showWordSelection(config, player, computerPlayer, false); 
        });

        gameButtonsPanel.add(multiplayer);
        gameButtonsPanel.add(solo);
        optionsPanel.add(gameButtonsPanel); // Add game buttons to the main options panel

        add(optionsPanel, BorderLayout.CENTER); // Add the combined options and game buttons panel to center

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }
    
    private GameConfig createConfig(GameMode mode, GamePlayer playerOne, GamePlayer playerTwo) {
        WordLength selectedWordLength = (WordLength) wordLengthComboBox.getSelectedItem();
        Difficulty selectedDifficulty = (Difficulty) difficultyComboBox.getSelectedItem();
        TimerDuration selectedTimerDuration = (TimerDuration) timerDurationComboBox.getSelectedItem();
        
        return new GameConfig(mode, selectedDifficulty, selectedWordLength, selectedTimerDuration, playerOne, playerTwo);
    }

    private WordChoice[] pickWords(WordLength length, int count) {
        WordChoice[] choices = new WordChoice[count];
        for (int i = 0; i < count; i++) {
            String word;
            int attempts = 0;
            // Ensure unique words if picking multiple
            do {
                word = gameController.pickWord(length);
                attempts++;
            } while (i > 0 && word.equalsIgnoreCase(choices[i - 1].word()) && attempts < 3); // Limit attempts to avoid infinite loop
            choices[i] = new WordChoice(word, WordSource.rollTheDice); // For now, always computer picks
        }
        return choices;
    }

    private final Navigation navigation;
    private final GameController gameController;
    static final long serialVersionUID = 1L;
}
