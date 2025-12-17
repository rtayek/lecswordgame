package view;
import controller.AppController;
import controller.DictionaryService;
import controller.GameController;
import javax.swing.SwingUtilities;
public class Main {
	public static void main(String[] args) {
		SwingUtilities.invokeLater(()-> {
			var appController=new AppController();
			var dictionaryService = new DictionaryService();
			var gameController=new GameController(dictionaryService);
			var frame=new MainFrame(appController,gameController);
			frame.setVisible(true);
		});
	}
}
