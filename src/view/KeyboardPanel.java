package view;

import java.awt.Color;
import java.awt.FlowLayout; // Import FlowLayout
import javax.swing.BoxLayout; // Import BoxLayout
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;
import model.Enums.Difficulty;
import model.Enums.LetterFeedback;
import model.Records.GuessResult;

class KeyboardPanel extends JPanel {

    private final Map<Character, JButton> buttons = new HashMap<>();
    private final Map<Character, LetterFeedback> letterStates = new HashMap<>();
    private final Consumer<Character> onKey;

    KeyboardPanel(Consumer<Character> onKey) {
        this.onKey = onKey;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Change to BoxLayout

        String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
        String[] row3 = {"Z", "X", "C", "V", "B", "N", "M"};

        add(createRowPanel(row1));
        add(createRowPanel(row2));
        add(createRowPanel(row3));
        
        // Initialize all letters as unused
        for (char c = 'A'; c <= 'Z'; c++) {
            letterStates.put(c, LetterFeedback.unused);
        }
    }
    
    private JPanel createRowPanel(String[] keys) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4)); // Use FlowLayout for rows
        for (String key : keys) {
            char letter = key.charAt(0);
            JButton button = new JButton(String.valueOf(letter));
            button.addActionListener(e -> onKey.accept(letter));
            buttons.put(letter, button);
            rowPanel.add(button);
        }
        return rowPanel;
    }

    void updateKeyboard(GuessResult result, Difficulty difficulty) {
        String guess = result.guess().toUpperCase();
        for (int i = 0; i < guess.length(); i++) {
            char c = guess.charAt(i);
            LetterFeedback newFeedback = result.feedback().get(i);
            
            // Update letter state if new feedback is "better"
            LetterFeedback currentFeedback = letterStates.getOrDefault(c, LetterFeedback.unused);
            if (getFeedbackPrecedence(newFeedback) > getFeedbackPrecedence(currentFeedback)) {
                letterStates.put(c, newFeedback);
            }
        }
        applyStyles(difficulty);
    }

    void resetKeyboard() {
        for (char c = 'A'; c <= 'Z'; c++) {
            letterStates.put(c, LetterFeedback.unused);
            JButton button = buttons.get(c);
            if (button != null) {
                button.setBackground(null); // Reset to default background
                button.setOpaque(false); // Make sure it's not opaque with null background
                button.setForeground(Color.BLACK); // Reset text color
            }
        }
        repaint();
    }

    private void applyStyles(Difficulty difficulty) {
        for (Map.Entry<Character, LetterFeedback> entry : letterStates.entrySet()) {
            char c = entry.getKey();
            LetterFeedback feedback = entry.getValue();
            JButton button = buttons.get(c);

            if (button != null) {
                Color bgColor = getButtonColor(feedback, difficulty);
                if (bgColor != null) {
                    button.setBackground(bgColor);
                    button.setOpaque(true);
                } else {
                    button.setBackground(null);
                    button.setOpaque(false);
                }
                button.setForeground(Color.WHITE); // Default text color for used letters
            }
        }
        repaint();
    }

    private Color getButtonColor(LetterFeedback feedback, Difficulty difficulty) {
        if (difficulty == Difficulty.expert) {
            return (feedback != LetterFeedback.unused) ? Color.GRAY : null; // Just gray out if used in Expert mode
        }

        // Normal and Hard modes
        switch (feedback) {
            case correctPosition:
                return new Color(0x2E7D32); // Green
            case wrongPosition:
                if (difficulty == Difficulty.hard) {
                    return new Color(0x2E7D32); // Green for "present in word" in Hard mode
                } else {
                    return new Color(0xF9A825); // Orange for "wrong position" in Normal mode
                }
            case notInWord:
                return new Color(0xB71C1C); // Red
            case unused:
            default:
                return null; // Default background (no specific color)
        }
    }

    // Define precedence for feedback types (higher means "better")
    private int getFeedbackPrecedence(LetterFeedback fb) {
        return switch (fb) {
            case correctPosition -> 3;
            case wrongPosition -> 2;
            case notInWord -> 1;
            case usedPresent, usedNotPresent, unused -> 0; // These are less specific or initial states
        };
    }

    static final long serialVersionUID = 1L;
}