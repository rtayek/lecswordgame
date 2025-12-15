package controller;

import java.util.ArrayList;
import java.util.List;
import model.GameLogEntry;
import model.PlayerProfile;

public class AppController {

    public AppController() {
        this.gameLog = new ArrayList<>();
    }

    public PlayerProfile getCurrentProfile() {
        return currentProfile;
    }

    public void setCurrentProfile(PlayerProfile currentProfile) {
        this.currentProfile = currentProfile;
    }

    public List<GameLogEntry> getGameLog() {
        return new ArrayList<>(gameLog);
    }

    public void addGameLogEntry(GameLogEntry entry) {
        gameLog.add(entry);
    }

    PlayerProfile currentProfile;
    final List<GameLogEntry> gameLog;
}
