package view;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class HardestWordsPanel extends JPanel {

    HardestWordsPanel(Navigation navigation) {
        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Hardest Words"), BorderLayout.NORTH);

        var text = new JTextArea("List of toughest words will be here.");
        text.setEditable(false);
        add(new JScrollPane(text), BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    private static final long serialVersionUID = 1L;
}
