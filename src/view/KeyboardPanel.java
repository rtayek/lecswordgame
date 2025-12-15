package view;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JPanel;

class KeyboardPanel extends JPanel {

    KeyboardPanel(Consumer<Character> onKey) {
        this.onKey = onKey;
        setLayout(new GridLayout(2, 13, 4, 4));
        createKeys();
    }

    private void createKeys() {
        for (char c = 'A'; c <= 'Z'; c++) {
            var letter = c;
            var button = new JButton(String.valueOf(letter));
            button.addActionListener(e -> onKey.accept(letter));
            buttons.put(letter, button);
            add(button);
        }
    }

    void setLetterUsed(char c, Color color) {
        var key = Character.toUpperCase(c);
        var button = buttons.get(key);
        if (button != null) {
            button.setBackground(color);
            button.setOpaque(true);
        }
    }

    void grayOutLetter(char c) {
        setLetterUsed(c, Color.LIGHT_GRAY);
    }

    private static final long serialVersionUID = 1L;
    private final Map<Character, JButton> buttons = new HashMap<>();
    private final Consumer<Character> onKey;
}
