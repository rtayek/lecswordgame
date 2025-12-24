package view;
import javax.swing.SwingUtilities;
import controller.AppController;
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(()-> {
			var appController = AppController.create(); // Build with default dependencies
			var frame=new MainFrame(appController); // Only appController goes to UI
			appController.setNavigation(frame);
			frame.setVisible(true);
		});
	}
}
