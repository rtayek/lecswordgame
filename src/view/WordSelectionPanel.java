package view;

import controller.AppController;
import controller.WordSelectionModel;
import controller.WordSelectionViewData;
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
import util.ResourceLoader; // Added: Import ResourceLoader
import controller.events.WordChoiceView;
import controller.events.WordLengthView;

class WordSelectionPanel extends JPanel {

    private final AppController appController;
    private WordSelectionViewData viewData;
    private final WordSelectionModel selectionModel = new WordSelectionModel();

    private JTextField wordInput;
    private JLabel wordLengthHint;
    private JLabel titleLabel;
    private JButton rollTheDiceButton;
    private JButton confirmWordButton;

    WordSelectionPanel(AppController appController, WordSelectionViewData data) {
        this.appController = appController;
        setContext(data);

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        titleLabel = new JLabel(titleText());
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(0, 1, 10, 10));

        // Word Input
        JPanel inputPanel = new JPanel(new FlowLayout());
        wordInput = new JTextField(viewData.wordLength());
        wordInput.setPreferredSize(new Dimension(150, 30));
        wordInput.setHorizontalAlignment(JTextField.CENTER);
        wordLengthHint = new JLabel("Word must be %d letters long.".formatted(viewData.wordLength()));
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
        ImageIcon diceIcon = ResourceLoader.getImageIcon("dice.png", 20, 20).orElse(null); // Use ResourceLoader and handle Optional
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

    void setContext(WordSelectionViewData data) {
        this.viewData = data;
        selectionModel.clear();
        if (titleLabel != null && data != null) {
            titleLabel.setText(titleText());
        }
        if (wordInput != null) {
            wordInput.setText("");
            if (data != null) {
                wordInput.setColumns(data.wordLength());
            }
        }
        if (wordLengthHint != null && data != null) {
            wordLengthHint.setText("Word must be %d letters long.".formatted(data.wordLength()));
        }
    }
    
    private String titleText() {
        return "Choose Word for " + (viewData != null ? viewData.opponentName() : "Opponent");
    }

    private void rollTheDice() {
        WordLengthView lengthEnum = WordLengthView.fromLength(viewData.wordLength());
        String chosenWord = appController.pickWord(lengthEnum).toUpperCase();
        selectionModel.recordRoll(chosenWord);
        wordInput.setText(chosenWord);
    }

    private void handleWordInputChange() {
        selectionModel.onInputChanged(wordInput.getText());
    }

    private void confirmWord() {
        String chosenWord = wordInput.getText().trim().toUpperCase();
        if (chosenWord.isEmpty()) {
            wordLengthHint.setText("Please enter a word or roll the dice.");
            return;
        }
        if (chosenWord.length() != viewData.wordLength()) {
            wordLengthHint.setText("Word must be %d letters long.".formatted(viewData.wordLength()));
            return;
        }
        WordLengthView lengthEnum = WordLengthView.fromLength(viewData.wordLength());
        if (!appController.isValidWord(chosenWord, lengthEnum)) {
             wordLengthHint.setText("'%s' is not a valid word.".formatted(chosenWord));
             return;
        }

        WordChoiceView wordChoice = selectionModel.buildChoice(chosenWord);
        selectionModel.clear();
        
        if (viewData.isPlayerOneTurn()) {
            appController.playerOneWordSelected(wordChoice);
        } else {
            appController.playerTwoWordSelected(wordChoice);
        }
    }

    private static final long serialVersionUID = 1L;
}
