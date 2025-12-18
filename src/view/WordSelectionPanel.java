package view;

import controller.AppController;
import controller.GameController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
// import java.awt.Image; // Removed: no longer needed for manual scaling
// import java.net.URL; // Removed: ResourceLoader handles URL
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import model.Enums.WordSource;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.WordChoice;
import util.ResourceLoader; // Added: Import ResourceLoader

class WordSelectionPanel extends JPanel {

    private final AppController appController;
    private final GameController gameController;
    private final GameConfig gameConfig;
    private final GamePlayer currentPlayer;
    private final GamePlayer opponentPlayer;
    private final boolean isPlayerOneTurn;
    private boolean rolledByDice = false;
    private String lastRolledWord;

    private JTextField wordInput;
    private JLabel wordLengthHint;
    private JButton rollTheDiceButton;
    private JButton confirmWordButton;

    WordSelectionPanel(AppController appController, GameController gameController, GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, boolean isPlayerOneTurn) {
        this.appController = appController;
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
        wordInput.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { handleWordInputChange(); }
            @Override
            public void removeUpdate(DocumentEvent e) { handleWordInputChange(); }
            @Override
            public void changedUpdate(DocumentEvent e) { handleWordInputChange(); }
        });
        inputPanel.add(new JLabel("Enter Word:"));
        inputPanel.add(wordInput);
        inputPanel.add(wordLengthHint);
        centerPanel.add(inputPanel);

        // Roll the Dice button
        rollTheDiceButton = new JButton("Roll the Dice");
        ImageIcon diceIcon = ResourceLoader.getImageIcon("dice_icon.png", 20, 20).orElse(null); // Use ResourceLoader and handle Optional
        if (diceIcon != null) {
            rollTheDiceButton.setIcon(diceIcon);
        } else {
            // Error message is handled by ResourceLoader
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
        String chosenWord = gameController.pickWord(gameConfig.wordLength()).toUpperCase();
        wordInput.setText(chosenWord);
        rolledByDice = true;
        lastRolledWord = chosenWord;
    }

    private void handleWordInputChange() {
        String current = wordInput.getText() == null ? "" : wordInput.getText().trim().toUpperCase();
        if (!current.equals(lastRolledWord)) {
            rolledByDice = false;
        }
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

        boolean usedDice = rolledByDice && chosenWord.equalsIgnoreCase(lastRolledWord);
        WordChoice wordChoice = new WordChoice(chosenWord, usedDice ? WordSource.rollTheDice : WordSource.manual);
        rolledByDice = false; // reset for next time
        lastRolledWord = null;
        
        // This is where we need to pass the word back to GameSetupPanel or start the game
        // For now, we navigate back, in a real scenario, this would trigger the next step of setup
        if (isPlayerOneTurn) {
            appController.playerOneWordSelected(wordChoice);
        } else {
            appController.playerTwoWordSelected(wordChoice);
        }
    }

    private static final long serialVersionUID = 1L;
}
