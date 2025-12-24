package view;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.util.Map;
import javax.swing.JButton;
import org.junit.jupiter.api.Test;

class KeyboardPanelTestCase {

    @SuppressWarnings("unchecked")
    @Test
    void setEnabledPropagatesToAllKeys() throws Exception {
        KeyboardPanel panel = new KeyboardPanel(c -> {});
        panel.setEnabled(false);

        Field buttonsField = KeyboardPanel.class.getDeclaredField("buttons");
        buttonsField.setAccessible(true);
        Map<Character, JButton> buttons = (Map<Character, JButton>) buttonsField.get(panel);

        assertFalse(panel.isEnabled(), "Panel should be disabled");
        for (Map.Entry<Character, JButton> e : buttons.entrySet()) {
            assertFalse(e.getValue().isEnabled(), "Button " + e.getKey() + " should be disabled");
        }
    }
}
