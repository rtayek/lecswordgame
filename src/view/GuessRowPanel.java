package view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Enums.LetterFeedback;

class GuessRowPanel extends JPanel {

    GuessRowPanel(String guess, java.util.List<LetterFeedback> feedback) {
        setLayout(new GridLayout(1, guess.length(), 5, 5));

        for (int i = 0; i < guess.length(); i++) {
            char c = guess.charAt(i);
            LetterFeedback fb = feedback != null && i < feedback.size() ? feedback.get(i) : null;
            add(createTile(c, fb));
        }
    }

    private JLabel createTile(char c, LetterFeedback fb) {
        JLabel label = new JLabel(String.valueOf(c).toUpperCase(), JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setOpaque(true);
        label.setForeground(Color.WHITE);

        Color bg = switch (fb) {
            case correctPosition -> new Color(0x2E7D32); // green
            case wrongPosition   -> new Color(0xF9A825); // yellow-orange
            case notInWord       -> new Color(0xB71C1C); // red
            case usedPresent     -> new Color(0x1565C0); // blue for "present but no position"
            case usedNotPresent  -> new Color(0x424242); // dark gray
            case unused          -> new Color(0x9E9E9E); // light gray
            default              -> new Color(0x9E9E9E); // null or any unexpected value
        };

        label.setBackground(bg);
        return label;
    }

    static final long serialVersionUID = 1L;
}
