package view;

import controller.AppController;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import model.Records.GameLogEntry;
import model.Enums.Difficulty;
import model.Enums.WordLength;

class GameLogPanel extends JPanel {

    private final AppController appController; // Add AppController
    private DefaultTableModel tableModel;
    private JTable gameLogTable;

    GameLogPanel(Navigation navigation, AppController appController) { // Update constructor
        this.appController = appController; // Initialize AppController

        setLayout(new BorderLayout(8, 8));
        
        JLabel titleLabel = new JLabel("Game Log");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"#", "Player 1", "Player 2", "Difficulty", "Word Length", "Result"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        gameLogTable = new JTable(tableModel);
        gameLogTable.setFillsViewportHeight(true); // Table fills the height of the scroll pane
        add(new JScrollPane(gameLogTable), BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    public void onShow() {
        tableModel.setRowCount(0); // Clear existing data
        List<GameLogEntry> logs = appController.getGameLog();

        int gameNum = 1;
        for (GameLogEntry entry : logs) {
            tableModel.addRow(new Object[]{
                gameNum++,
                entry.playerOneName(),
                entry.playerTwoName(),
                entry.difficulty(),
                entry.wordLength(),
                entry.resultSummary()
            });
        }
    }

    static final long serialVersionUID = 1L;
}
