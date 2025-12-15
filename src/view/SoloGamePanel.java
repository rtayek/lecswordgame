package view;

import controller.GameController;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class SoloGamePanel extends JPanel {

    SoloGamePanel(Navigation navigation, GameController gameController) {
        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Solo Game"), BorderLayout.NORTH);

        statusArea = new JTextArea("Play against the computer.\n");
        statusArea.setEditable(false);
        add(new JScrollPane(statusArea), BorderLayout.CENTER);

        keyboardPanel = new KeyboardPanel(c -> statusArea.append(c + ""));
        add(keyboardPanel, BorderLayout.SOUTH);

        var controls = new JPanel();
        var submit = new JButton("Submit Guess");
        submit.addActionListener(e -> {
            gameController.submitGuess(null, "");
            statusArea.append("Guess submitted.\n");
        });
        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        controls.add(submit);
        controls.add(back);
        add(controls, BorderLayout.WEST);
    }

    KeyboardPanel getKeyboardPanel() {
        return keyboardPanel;
    }

    private static final long serialVersionUID = 1L;

    private final JTextArea statusArea;
    private final KeyboardPanel keyboardPanel;
}
