package view;

import controller.GameController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import model.Enums.WordSource;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.WordChoice;

class WordSelectionPanel extends JPanel {

    private final Navigation navigation;
    private final GameController gameController;
    private final GameConfig gameConfig;
    private final GamePlayer currentPlayer;
    private final GamePlayer opponentPlayer;
    private final boolean isPlayerOneTurn;

    private JTextField wordInput;
    private JLabel wordLengthHint;
    private JButton rollTheDiceButton;
    private JButton confirmWordButton;

    WordSelectionPanel(Navigation navigation, GameController gameController, GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, boolean isPlayerOneTurn) {
        this.navigation = navigation;
        this.gameController = gameController;
        this.gameConfig = config;
        this.isPlayerOneTurn = isPlayerOneTurn;
        this.currentPlayer = isPlayerOneTurn ? playerOne : playerTwo;
        this.opponentPlayer = isPlayerOneTurn ? playerTwo : playerOne;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Choose Word for " + opponentPlayer.profile().username());
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        // Word Input
        JPanel inputPanel = new JPanel(new FlowLayout());
        wordInput = new JTextField(gameConfig.wordLength().length());
        wordInput.setPreferredSize(new Dimension(150, 30));
        wordInput.setHorizontalAlignment(JTextField.CENTER);
        wordLengthHint = new JLabel("Word must be %d letters long.".formatted(gameConfig.wordLength().length()));
        inputPanel.add(new JLabel("Enter Word:"));
        inputPanel.add(wordInput);
        inputPanel.add(wordLengthHint);
        centerPanel.add(inputPanel);

        // Roll the Dice button
        rollTheDiceButton = new JButton("Roll the Dice");
        URL diceImageUrl = getClass().getResource("/main/resources/dice_icon.png"); // Assuming a dice icon
        if (diceImageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(diceImageUrl);
            Image scaledImage = originalIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            rollTheDiceButton.setIcon(new ImageIcon(scaledImage));
        } else {
            System.err.println("Dice icon image not found at /main/resources/dice_icon.png");
        }
        rollTheDiceButton.addActionListener(e -> rollTheDice());
        centerPanel.add(rollTheDiceButton);

        add(centerPanel, BorderLayout.CENTER);

        // Confirm Button
        confirmWordButton = new JButton("Confirm Word");
        confirmWordButton.addActionListener(e -> confirmWord());
        add(confirmWordButton, BorderLayout.SOUTH);
    }

    private void rollTheDice() {
        String chosenWord = gameController.pickWord(gameConfig.wordLength());
        wordInput.setText(chosenWord);
    }

    private void confirmWord() {
        String chosenWord = wordInput.getText().trim().toUpperCase();
        if (chosenWord.isEmpty()) {
            wordLengthHint.setText("Please enter a word or roll the dice.");
            return;
        }
        if (chosenWord.length() != gameConfig.wordLength().length()) {
            wordLengthHint.setText("Word must be %d letters long.".formatted(gameConfig.wordLength().length()));
            return;
        }
        if (!gameController.isValidWord(chosenWord, gameConfig.wordLength())) {
             wordLengthHint.setText("'%s' is not a valid word.".formatted(chosenWord));
             return;
        }

        WordChoice wordChoice = new WordChoice(chosenWord, WordSource.manual); // Assume manual if entered
        if (wordInput.getText().equals(gameController.pickWord(gameConfig.wordLength()))) { // Simple check if it was 'rolled'
            wordChoice = new WordChoice(chosenWord, WordSource.rollTheDice);
        }
        
        // This is where we need to pass the word back to GameSetupPanel or start the game
        // For now, we navigate back, in a real scenario, this would trigger the next step of setup
        if (isPlayerOneTurn) {
            navigation.playerOneWordSelected(wordChoice);
        } else {
            navigation.playerTwoWordSelected(wordChoice);
        }
    }

    private static final long serialVersionUID = 1L;
}
