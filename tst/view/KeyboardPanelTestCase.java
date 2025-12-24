package view;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class KeyboardPanelTestCase {

    @Test
    void setEnabledPropagatesToAllKeys() {
        KeyboardPanel panel = new KeyboardPanel(c -> {});
        panel.setEnabled(false);

        assertFalse(panel.isEnabled(), "Panel should be disabled");
        for (var button : panel.debugButtons()) {
            assertFalse(button.isEnabled(), "Button should be disabled");
        }
    }
}
