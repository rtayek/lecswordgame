package controller;

import java.util.ArrayList;
import java.util.List;
import model.Records.GameLogEntry;
import model.Records.HardWordEntry;
import model.Records.PlayerProfile;
import util.PersistenceService;

/**
 * Wraps persistence decisions for profile, logs, and hardest words.
 */
public class ProfileService {

    private final PersistenceService persistenceService;
    private PlayerProfile currentProfile;
    private final List<GameLogEntry> gameLogs;

    public ProfileService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        this.currentProfile = persistenceService.loadPlayerProfile();
        this.gameLogs = new ArrayList<>(persistenceService.loadGameLogs());
    }

    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }

    public void saveProfile(PlayerProfile profile) {
        this.currentProfile = profile;
        persistenceService.savePlayerProfile(profile);
    }

    public List<GameLogEntry> getGameLogs() {
        return new ArrayList<>(gameLogs);
    }

    public void addGameLogEntry(GameLogEntry entry) {
        gameLogs.add(entry);
        persistenceService.saveGameLogs(gameLogs);
    }

    public List<HardWordEntry> getHardestWords() {
        return persistenceService.loadHardestWords();
    }
}
