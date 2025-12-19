package util;

import java.net.URL;

import javax.swing.ImageIcon;

/**
 * Simple resource loader smoke test without JUnit dependency.
 */
public final class ResourceLoaderTestCase {

    public static void main(String[] args) {
        shouldLoadAppPng();
        System.out.println("ResourceLoaderTestCase passed");
    }

    private static void shouldLoadAppPng() {
        String path = "app.png";
        URL imageUrl = Thread.currentThread().getContextClassLoader().getResource(path);
        if (imageUrl == null) {
            throw new AssertionError("app.png should be on classpath");
        }

        ImageIcon originalIcon = new ImageIcon(imageUrl);
        if (originalIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
            throw new AssertionError("Image should load");
        }

        var scaled = ResourceLoader.getScaledImage(originalIcon.getImage(), 32, 32);
        if (scaled == null) {
            throw new AssertionError("Scaled image should be returned");
        }
    }
}
