package view;

import controller.AppController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.Records.PlayerProfile;

class ProfileSetupPanel extends JPanel {

    private final Navigation navigation;
    private final AppController appController; // Add AppController
    private JTextField usernameField;
    private JLabel profilePictureLabel;
    private String currentAvatarPath; // To store the path of the selected avatar

    ProfileSetupPanel(Navigation navigation, AppController appController) { // Update constructor
        this.navigation = navigation;
        this.appController = appController; // Initialize AppController

        setLayout(new BorderLayout(15, 15)); // Add some padding
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Profile Setup");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Center Panel for Username and Profile Picture
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Username section
        JPanel usernamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        usernamePanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15); // Will be populated from loaded profile
        usernamePanel.add(usernameField);
        centerPanel.add(usernamePanel, BorderLayout.NORTH);

        // Profile Picture section
        JPanel picturePanel = new JPanel(new BorderLayout(5, 5));
        picturePanel.add(new JLabel("Profile Picture:"), BorderLayout.NORTH);

        profilePictureLabel = new JLabel();
        profilePictureLabel.setPreferredSize(new Dimension(100, 100));
        profilePictureLabel.setHorizontalAlignment(JLabel.CENTER);
        profilePictureLabel.setVerticalAlignment(JLabel.CENTER);
        profilePictureLabel.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.BLACK));
        // loadDefaultProfilePicture(); // Load from profile instead
        picturePanel.add(profilePictureLabel, BorderLayout.CENTER);

        JButton choosePictureButton = new JButton("Choose Picture");
        choosePictureButton.addActionListener(e -> chooseProfilePicture());
        picturePanel.add(choosePictureButton, BorderLayout.SOUTH);

        centerPanel.add(picturePanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Save and Back Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Profile");
        saveButton.addActionListener(e -> saveProfile());
        buttonPanel.add(saveButton);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        buttonPanel.add(back);
        add(buttonPanel, BorderLayout.SOUTH);

        loadProfileData(); // Load profile data after UI is set up
    }

    private void loadProfileData() {
        PlayerProfile profile = appController.getCurrentProfile();
        usernameField.setText(profile.username());
        currentAvatarPath = profile.avatarPath();
        if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
            ImageIcon icon = new ImageIcon(currentAvatarPath);
            setProfilePicture(icon);
        } else {
            loadDefaultProfilePicture(); // Fallback to default if no path or path is empty
        }
    }

    private void saveProfile() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        PlayerProfile newProfile = new PlayerProfile(username, currentAvatarPath);
        appController.setCurrentProfile(newProfile);
        JOptionPane.showMessageDialog(this, "Profile saved successfully!");
    }

    private void loadDefaultProfilePicture() {
        // Try to load a default avatar from resources
        URL defaultImageUrl = getClass().getResource("/main/resources/default_avatar.png");
        if (defaultImageUrl != null) {
            ImageIcon defaultIcon = new ImageIcon(defaultImageUrl);
            setProfilePicture(defaultIcon);
            // currentAvatarPath = defaultImageUrl.getPath(); // Don't set path for default
        } else {
            profilePictureLabel.setText("No Image");
            System.err.println("Default avatar image not found at /main/resources/default_avatar.png");
            currentAvatarPath = null;
        }
    }

    private void chooseProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "gif"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(selectedFile.getAbsolutePath());
            setProfilePicture(icon);
            currentAvatarPath = selectedFile.getAbsolutePath(); // Store absolute path
        }
    }

    private void setProfilePicture(ImageIcon originalIcon) {
        if (originalIcon.getImage() == null) {
            profilePictureLabel.setText("Invalid Image");
            currentAvatarPath = null;
            return;
        }
        Image scaledImage = getScaledImage(originalIcon.getImage(), 100, 100);
        profilePictureLabel.setIcon(new ImageIcon(scaledImage));
        profilePictureLabel.setText(""); // Clear text if image is set
    }

    // Helper to scale image while maintaining aspect ratio
    private Image getScaledImage(Image srcImg, int w, int h){
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }

    // getUsername and getAvatarPath are no longer needed as AppController handles saving/loading
    /*
    public String getUsername() {
        return usernameField.getText();
    }

    public String getAvatarPath() {
        return currentAvatarPath;
    }
    */

    private static final long serialVersionUID = 1L;
}
