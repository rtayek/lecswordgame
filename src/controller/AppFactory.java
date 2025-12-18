package controller;

import util.PersistenceService;

/**
 * Builds the application controller with default dependencies.
 */
public final class AppFactory {
    private AppFactory() {}

    public static AppController create() {
        var persistenceService = new PersistenceService();
        var wordService = new DictionaryService();
        var timerController = new TimerController();
        var gameController = new GameController(wordService);
        return new AppController(persistenceService, gameController, timerController);
    }
}
