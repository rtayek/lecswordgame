package util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

import javax.swing.ImageIcon;

import org.junit.jupiter.api.Test;

class ResourceLoaderTestCase {

    @Test
    void shouldLoadAppPng() {
        String path = "app.png";
        URL imageUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        assertNotNull(imageUrl, "app.png should be on classpath");

        ImageIcon originalIcon = new ImageIcon(imageUrl);
        assertTrue(originalIcon.getImageLoadStatus() != java.awt.MediaTracker.ERRORED, "Image should load");

        var scaled = ResourceLoader.getScaledImage(originalIcon.getImage(), 32, 32);
        assertNotNull(scaled, "Scaled image should be returned");
    }
}
