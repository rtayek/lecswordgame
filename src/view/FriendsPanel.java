package view;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

class FriendsPanel extends JPanel {

    FriendsPanel(Navigation navigation) {
        setLayout(new BorderLayout(8, 8));
        var friendsList = new JList<>(new String[]{"Alice", "Bob", "Charlie"});
        add(new JScrollPane(friendsList), BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    static final long serialVersionUID = 1L;
}
