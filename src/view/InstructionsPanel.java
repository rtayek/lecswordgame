package view;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

class InstructionsPanel extends JPanel {

    InstructionsPanel(Navigation navigation) {
        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Instructions"), BorderLayout.NORTH);

        var text = new JTextArea("How to play will appear here.");
        text.setEditable(false);
        add(new JScrollPane(text), BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    private static final long serialVersionUID = 1L;
}
