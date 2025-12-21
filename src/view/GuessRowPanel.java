package view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import controller.events.GuessResultView;
import controller.events.LetterFeedbackView;
import controller.events.DifficultyView;

class GuessRowPanel extends JPanel {

    private final DifficultyView difficulty; // Store as instance variable

    GuessRowPanel(GuessResultView result, DifficultyView difficulty) {
        this.difficulty = difficulty; // Initialize
        if (difficulty == DifficultyView.expert) {
            setupExpert(result);
        } else {
            setupNormal(result);
        }
    }

    private void setupExpert(GuessResultView result) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        var label = new JLabel(
            "%s [%d]".formatted(result.guess(), result.correctLetterCount())
        );
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        add(label);
    }

    private void setupNormal(GuessResultView result) {
        var guess = result.guess();
        var feedback = result.feedback();
        setLayout(new GridLayout(1, guess.length(), 5, 5));

        for (int i = 0; i < guess.length(); i++) {
            char c = guess.charAt(i);
            LetterFeedbackView fb = feedback != null && i < feedback.size() ? feedback.get(i) : null;
            add(createTile(c, fb));
        }
    }

    private JLabel createTile(char c, LetterFeedbackView fb) {
        JLabel label = new JLabel(String.valueOf(c).toUpperCase(), JLabel.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setOpaque(true);
        label.setForeground(Color.WHITE);

        Color bg;
        if (difficulty == DifficultyView.hard) {
            bg = switch (fb) {
                case correct, present -> new Color(0x2E7D32); // green for present in word
                case absent           -> new Color(0xB71C1C); // red
                default               -> new Color(0x9E9E9E); // default gray
            };
        } else { // Normal difficulty or others
            bg = switch (fb) {
                case correct    -> new Color(0x2E7D32); // green
                case present    -> new Color(0xF9A825); // yellow-orange
                case absent     -> new Color(0xB71C1C); // red
                case unused     -> new Color(0x9E9E9E); // light gray
                default         -> new Color(0x9E9E9E); // null or any unexpected value
            };
        }

        label.setBackground(bg);
        return label;
    }

    static final long serialVersionUID = 1L;
}
