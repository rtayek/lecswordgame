package util;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
public class ResourceLoaderTestCase {
	public static void main(String[] args) {
		String path="app.png";
		URL imageUrl=Thread.currentThread().getContextClassLoader().getResource(path);
		System.out.println(imageUrl);
        if (imageUrl != null) {
            ImageIcon originalIcon = new ImageIcon(imageUrl);
            if (originalIcon.getImageLoadStatus() == java.awt.MediaTracker.ERRORED) {
                System.err.println("Error loading image: " + path);
            }
            Image scaledImage = ResourceLoader.getScaledImage(originalIcon.getImage(), 32,32);
            System.out.println("got image: " + path);
        } else {
            System.err.println("Image not found: /" + path); // Corrected error message
        }

	}
}
