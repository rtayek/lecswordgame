package util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URL;

import javax.swing.ImageIcon;
import org.junit.jupiter.api.Test;

/**
 * Resource loader smoke test using JUnit 5.
 */
public final class ResourceLoaderTestCase {

    @Test
    void shouldLoadAndScaleAppPng() {
        String path = "app.png";
        URL imageUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        assertNotNull(imageUrl, "app.png should be on classpath");

        ImageIcon originalIcon = new ImageIcon(imageUrl);
        assertNotEquals(java.awt.MediaTracker.ERRORED, originalIcon.getImageLoadStatus(), "Image should load");

        var scaled = ResourceLoader.getScaledImage(originalIcon.getImage(), 32, 32);
        assertNotNull(scaled, "Scaled image should be returned");
    }
}
