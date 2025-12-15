package view;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class GameLogPanel extends JPanel {

    GameLogPanel(Navigation navigation) {
        setLayout(new BorderLayout(8, 8));
        var logs = new JList<>(new String[]{"No games yet"});
        add(new JScrollPane(logs), BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    static final long serialVersionUID = 1L;
}
