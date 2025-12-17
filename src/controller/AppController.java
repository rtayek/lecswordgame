package controller;

import java.util.ArrayList;
import java.util.List;
import model.Records.GameLogEntry;
import model.Records.PlayerProfile;
import util.PersistenceService;

public class AppController {

    private final PersistenceService persistenceService;

    public AppController(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        this.currentProfile = persistenceService.loadPlayerProfile(); // Load on startup
        this.gameLog = persistenceService.loadGameLogs(); // Load on startup
    }

    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(PlayerProfile currentProfile) {
        this.currentProfile = currentProfile;
        persistenceService.savePlayerProfile(currentProfile); // Save immediately
    }

    public List<GameLogEntry> getGameLog() {
        return new ArrayList<>(gameLog);
    }

    public void addGameLogEntry(GameLogEntry entry) {
        gameLog.add(entry);
        persistenceService.saveGameLogs(gameLog); // Save immediately
    }

    PlayerProfile currentProfile;
    final List<GameLogEntry> gameLog;
}
