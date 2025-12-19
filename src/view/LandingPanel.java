package view;

import controller.api.Navigation;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import util.ResourceLoader; // Import ResourceLoader

class LandingPanel extends JPanel {

    LandingPanel(Navigation navigation) {
        setLayout(new BorderLayout());
        setBackground(new java.awt.Color(0x102542)); // midnight blue

        JLabel logoLabel;
        ImageIcon appLogoIcon = ResourceLoader.getImageIcon("app.png", 200, 200).orElse(null); // Use ResourceLoader and handle Optional
        if (appLogoIcon != null) {
            logoLabel = new JLabel(appLogoIcon);
        } else {
            logoLabel = new JLabel("App Logo Placeholder");
            logoLabel.setPreferredSize(new Dimension(200, 200)); // Give it some size
            // Error message is handled by ResourceLoader
        }
        logoLabel.setHorizontalAlignment(JLabel.CENTER);
        add(logoLabel, BorderLayout.NORTH);

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
