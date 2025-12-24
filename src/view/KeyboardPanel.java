package view;

import java.awt.Color;
import java.awt.FlowLayout; // Import FlowLayout
import javax.swing.BoxLayout; // Import BoxLayout
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;
import controller.events.DifficultyView;
import controller.events.KeyboardView;
import controller.events.LetterFeedbackView;

class KeyboardPanel extends JPanel {

    private final Map<Character, JButton> buttons = new HashMap<>();
    private final Map<Character, LetterFeedbackView> letterStates = new HashMap<>();
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
            letterStates.put(c, LetterFeedbackView.unused);
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

    void resetKeyboard() {
        for (char c = 'A'; c <= 'Z'; c++) {
            letterStates.put(c, LetterFeedbackView.unused);
            JButton button = buttons.get(c);
            if (button != null) {
                button.setBackground(null); // Reset to default background
                button.setOpaque(false); // Make sure it's not opaque with null background
                button.setForeground(Color.BLACK); // Reset text color
            }
        }
        repaint();
    }

    private void applyStyles(DifficultyView difficulty) {
        for (Map.Entry<Character, LetterFeedbackView> entry : letterStates.entrySet()) {
            char c = entry.getKey();
            LetterFeedbackView feedback = entry.getValue();
            JButton button = buttons.get(c);

            if (button != null) {
                Color bgColor = getButtonColor(feedback, difficulty);
                if (bgColor != null) {
                    button.setBackground(bgColor);
                    button.setOpaque(true);
                    button.setForeground(Color.WHITE);
                } else {
                    button.setBackground(null);
                    button.setOpaque(false);
                    button.setForeground(Color.BLACK);
                }
            }
        }
        repaint();
    }

    private Color getButtonColor(LetterFeedbackView feedback, DifficultyView difficulty) {
        if (difficulty == DifficultyView.expert) {
            return (feedback != LetterFeedbackView.unused) ? Color.GRAY : null; // Just gray out if used in Expert mode
        }

        // Normal and Hard modes
        switch (feedback) {
            case correct:
                return new Color(0x2E7D32); // Green
            case present:
                if (difficulty == DifficultyView.hard) {
                    return new Color(0x2E7D32); // Green for "present in word" in Hard mode
                } else {
                    return new Color(0xF9A825); // Orange for "wrong position" in Normal mode
                }
            case absent:
                return new Color(0xB71C1C); // Red
            case unused:
            default:
                return null; // Default background (no specific color)
        }
    }

    void apply(KeyboardView keyboardView, controller.events.DifficultyView difficultyValue) {
        // reset to unused
        for (char c = 'A'; c <= 'Z'; c++) {
            letterStates.put(c, LetterFeedbackView.unused);
        }
        if (keyboardView != null && keyboardView.keyStates() != null) {
            for (Map.Entry<Character, LetterFeedbackView> e : keyboardView.keyStates().entrySet()) {
                var fb = e.getValue() == null ? LetterFeedbackView.unused : e.getValue();
                letterStates.put(Character.toUpperCase(e.getKey()), fb);
            }
        }
        applyStyles(difficultyValue == null ? DifficultyView.normal : difficultyValue);
    }

    static final long serialVersionUID = 1L;

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        buttons.values().forEach(b -> b.setEnabled(enabled));
    }

    // Package-private test hook to avoid reflection in tests.
    java.util.Collection<JButton> debugButtons() {
        return java.util.Collections.unmodifiableCollection(buttons.values());
    }
}
