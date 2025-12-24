package view;

import javax.swing.SwingUtilities;

/**
 * EDT helper for view code. Controllers/services may call listeners from any thread;
 * views must marshal UI updates to the EDT.
 */
final class UiThread {
    static void run(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }

    private UiThread() { }
}
