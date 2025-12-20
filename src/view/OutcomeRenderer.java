package view;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import controller.OutcomeViewModel;
import util.ResourceLoader;

/**
 * Renders outcome view models (dialogs, sounds, graphics).
 */
public final class OutcomeRenderer {
    private OutcomeRenderer() {}

    public static void render(JComponent parent, OutcomeViewModel vm) {
        if (vm == null) return;

        if (vm.soundEffect() != null) {
            ResourceLoader.playSound(vm.soundEffect());
        }
        ImageIcon graphic = vm.graphicFile() != null
                ? ResourceLoader.getImageIcon(vm.graphicFile(), 100, 100).orElse(null)
                : null;
        JOptionPane.showMessageDialog(
                parent,
                vm.message(),
                vm.title(),
                JOptionPane.INFORMATION_MESSAGE,
                graphic
        );
    }
}
