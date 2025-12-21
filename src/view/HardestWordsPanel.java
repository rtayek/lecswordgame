package view;

import controller.AppController;
import controller.api.Navigation;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import controller.events.HardWordEntryView;

class HardestWordsPanel extends JPanel {

    private final AppController appController;
    private DefaultTableModel tableModel;
    private JTable hardestWordsTable;

    HardestWordsPanel(Navigation navigation, AppController appController) {
        this.appController = appController;

        setLayout(new BorderLayout(8, 8));
        setBackground(new java.awt.Color(0xE8F5E9)); // light green
        
        JLabel titleLabel = new JLabel("Hardest Words");
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"Rank", "Word", "Hardness Score"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        hardestWordsTable = new JTable(tableModel);
        hardestWordsTable.setFillsViewportHeight(true); // Table fills the height of the scroll pane
        add(new JScrollPane(hardestWordsTable), BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    public void onShow() {
        tableModel.setRowCount(0); // Clear existing data
        List<HardWordEntryView> hardWords = appController.getHardestWords();

        for (HardWordEntryView entry : hardWords) {
            tableModel.addRow(new Object[]{
                entry.rank(),
                entry.word(),
                entry.hardnessScore()
            });
        }
    }

    private static final long serialVersionUID = 1L;
}
