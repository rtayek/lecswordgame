package view;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.Rectangle;

class GuessGridPanel extends JPanel implements Scrollable {

    GuessGridPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    void addGuessRow(GuessRowPanel row) {
        add(row);
        revalidate();
        repaint();
    }

    void clearRows() {
        removeAll();
        revalidate();
        repaint();
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 20;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 40;
    }

    static final long serialVersionUID = 1L;
}
