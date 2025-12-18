package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

class FriendsPanel extends JPanel {

    private DefaultListModel<String> friendsListModel;
    private JTextField addFriendTextField;

    FriendsPanel(Navigation navigation) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new java.awt.Color(0xF3E5F5)); // light lavender

        JLabel titleLabel = new JLabel("Friends");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Display current friends
        friendsListModel = new DefaultListModel<>();
        friendsListModel.addElement("Alice");
        friendsListModel.addElement("Bob");
        friendsListModel.addElement("Charlie");
        JList<String> friendsList = new JList<>(friendsListModel);
        centerPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);

        // Add Friend section
        JPanel addFriendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        addFriendTextField = new JTextField(20);
        JButton addFriendButton = new JButton("Add Friend");
        addFriendButton.addActionListener(e -> addFriend());
        addFriendPanel.add(new JLabel("Add by Username/Link:"));
        addFriendPanel.add(addFriendTextField);
        addFriendPanel.add(addFriendButton);
        centerPanel.add(addFriendPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Bottom panel for Back and Search in Contacts
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton searchContactsButton = new JButton("Search in Contacts");
        searchContactsButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Search in contacts feature to be implemented."));
        bottomPanel.add(searchContactsButton);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        bottomPanel.add(back);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addFriend() {
        String friendName = addFriendTextField.getText().trim();
        if (!friendName.isEmpty() && !friendsListModel.contains(friendName)) {
            friendsListModel.addElement(friendName);
            addFriendTextField.setText("");
            JOptionPane.showMessageDialog(this, friendName + " added to your friends list!");
        } else if (friendsListModel.contains(friendName)) {
            JOptionPane.showMessageDialog(this, friendName + " is already in your friends list.");
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a valid username or link.");
        }
    }

    static final long serialVersionUID = 1L;
}
