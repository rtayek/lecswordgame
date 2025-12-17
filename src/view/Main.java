package view;
import controller.AppController;
import controller.DictionaryService;
import controller.GameController;
import controller.TimerController;
import util.PersistenceService; // Import PersistenceService
import javax.swing.SwingUtilities;
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(()-> {
			var persistenceService = new PersistenceService(); // Instantiate PersistenceService
			var appController=new AppController(persistenceService); // Pass to AppController
			var dictionaryService = new DictionaryService();
			var timerController = new TimerController();
			var gameController=new GameController(dictionaryService, timerController);
			var frame=new MainFrame(appController, gameController, timerController, persistenceService); // Pass to MainFrame
			frame.setVisible(true);
		});
	}
}
