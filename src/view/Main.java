package view;
import controller.AppController;
import controller.AppFactory;
import javax.swing.SwingUtilities;
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(()-> {
			var appController = AppFactory.create(); // Build with default dependencies
			var frame=new MainFrame(appController); // Only appController goes to UI
			appController.setNavigation(frame);
			frame.setVisible(true);
		});
	}
}
