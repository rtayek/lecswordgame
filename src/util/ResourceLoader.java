package util;

import java.util.Optional;
import util.SoundEffect;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public final class ResourceLoader {

    private ResourceLoader() {
        // Private constructor to prevent instantiation
    }

    public static Optional<ImageIcon> getImageIcon(String path, int width, int height) {
        URL imageUrl = ResourceLoader.class.getResource("/" + path); // Always prepend / for absolute classpath lookup
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            if (originalIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                System.err.println("Error loading image: " + path);
                return Optional.empty();
            }
            Image scaledImage = getScaledImage(originalIcon.getImage(), width, height);
            System.out.println("got image: " + path);
            return Optional.of(new ImageIcon(scaledImage));
        } else {
            System.err.println("Image not found: /" + path); // Corrected error message
            return Optional.empty();
        }
    }

    public static void playSound(SoundEffect soundEffect) {
        String fileName = soundEffect.getFileName();
        URL soundUrl = ResourceLoader.class.getResource(Constants.RESOURCES_PATH + fileName);
        if (soundUrl != null) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Error playing sound file " + fileName + ": " + e.getMessage());
            }
        } else {
            System.err.println("Sound file not found: " + Constants.RESOURCES_PATH + fileName);
        }
    }

    // Helper to scale image while maintaining aspect ratio
    static Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }
}
