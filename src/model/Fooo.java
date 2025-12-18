package model;

import javax.swing.ImageIcon;
import util.ResourceLoader;
import java.util.Optional;

public class Fooo {

    public static void main(String[] args) {
        // Attempt to load the small.png image
        Optional<ImageIcon> smallImage = ResourceLoader.getImageIcon("small.png", 100, 100);

        if (smallImage.isPresent()) {
            System.out.println("Image 'small.png' loaded successfully!");
            // You can further use smallImage.get() here, e.g., to display it in a JFrame
        } else {
            System.err.println("Failed to load image 'small.png'. Check if the file exists and is accessible.");
        }
    }
}
