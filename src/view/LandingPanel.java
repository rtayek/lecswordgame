package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

class LandingPanel extends JPanel {

    LandingPanel(Navigation navigation) {
        setLayout(new BorderLayout());

        JLabel logoLabel;
        URL imageUrl = getClass().getResource("/main/resources/app_logo.png");
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            // Scale the image to fit, e.g., 200x200
            Image scaledImage = originalIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledImage));
        } else {
            logoLabel = new JLabel("App Logo Placeholder");
            logoLabel.setPreferredSize(new Dimension(200, 200)); // Give it some size
            System.err.println("App logo image not found at /main/resources/app_logo.png");
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
