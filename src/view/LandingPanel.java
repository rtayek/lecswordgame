package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

class LandingPanel extends JPanel {

    LandingPanel(Navigation navigation) {
        setLayout(new BorderLayout());
        add(new JLabel("Word Guessing Game"), BorderLayout.NORTH);

        var buttonPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        var profile = new JButton("Profile Setup");
        profile.addActionListener(e -> navigation.showProfileSetup());
        var setup = new JButton("Game Setup");
        setup.addActionListener(e -> navigation.showGameSetup());
        var instructions = new JButton("Instructions");
        instructions.addActionListener(e -> navigation.showInstructions());
        var friends = new JButton("Friends");
        friends.addActionListener(e -> navigation.showFriends());
        var log = new JButton("Game Log");
        log.addActionListener(e -> navigation.showGameLog());
        var hardest = new JButton("Hardest Words");
        hardest.addActionListener(e -> navigation.showHardestWords());

        buttonPanel.add(profile);
        buttonPanel.add(setup);
        buttonPanel.add(instructions);
        buttonPanel.add(friends);
        buttonPanel.add(log);
        buttonPanel.add(hardest);

        add(buttonPanel, BorderLayout.CENTER);
    }

    private static final long serialVersionUID = 1L;
}
