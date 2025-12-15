package view;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class ProfileSetupPanel extends JPanel {

    ProfileSetupPanel(Navigation navigation) {
        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Profile Setup"), BorderLayout.NORTH);

        var usernameField = new JTextField("Player1");
        add(usernameField, BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    private static final long serialVersionUID = 1L;
}
