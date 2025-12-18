package util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import model.Records.GameLogEntry;
import model.Records.PlayerProfile;
import model.enums.Difficulty;
import model.enums.WordLength;
import model.Records.HardWordEntry;

public class PersistenceService {

    private static final String profileFile = "profile.properties";
    private static final String gameLogFile = "game_log.txt";
    private static final String hardeestWordFile = "hardest_words.txt";

    private final Path dataDirectory;

    public PersistenceService() {
        // Define a data directory within the user's home or application specific folder
        // For simplicity, using current directory for now, but should be more robust
        this.dataDirectory = Paths.get(System.getProperty("user.dir"), "data");
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                System.err.println("Error creating data directory: " + e.getMessage());
            }
        }
    }

    // --- Player Profile Persistence ---
    public void savePlayerProfile(PlayerProfile profile) {
        Properties properties = new Properties();
        if (profile != null) {
            properties.setProperty("username", profile.username());
            properties.setProperty("avatarPath", profile.avatarPath() != null ? profile.avatarPath() : "");
        }
        try (OutputStream os = Files.newOutputStream(dataDirectory.resolve(profileFile))) {
            properties.store(os, "Player Profile");
        } catch (IOException e) {
            System.err.println("Error saving player profile: " + e.getMessage());
        }
    }

    public PlayerProfile loadPlayerProfile() {
        Properties properties = new Properties();
        Path profilePath = dataDirectory.resolve(profileFile);
        if (Files.exists(profilePath)) {
            try (InputStream is = Files.newInputStream(profilePath)) {
                properties.load(is);
                String username = properties.getProperty("username", "Guest");
                String avatarPath = properties.getProperty("avatarPath", "");
                return new PlayerProfile(username, avatarPath);
            } catch (IOException e) {
                System.err.println("Error loading player profile: " + e.getMessage());
            }
        }
        return new PlayerProfile("Guest", ""); // Default profile
    }

    // --- Game Log Persistence ---
    public void saveGameLogs(List<GameLogEntry> logs) {
        try (BufferedWriter writer = Files.newBufferedWriter(dataDirectory.resolve(gameLogFile))) {
            for (GameLogEntry entry : logs) {
                writer.write(String.join(",",
                        entry.gameId(),
                        entry.playerOneName(),
                        entry.playerTwoName(),
                        entry.difficulty().name(),
                        entry.wordLength().name(),
                        entry.resultSummary()
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving game logs: " + e.getMessage());
        }
    }

    public List<GameLogEntry> loadGameLogs() {
        Path logPath = dataDirectory.resolve(gameLogFile);
        if (Files.exists(logPath)) {
            try (BufferedReader reader = Files.newBufferedReader(logPath)) {
                return reader.lines()
                        .map(line -> line.split(",", -1))
                        .filter(parts -> parts.length == 6)
                        .map(parts -> new GameLogEntry(
                                parts[0],
                                parts[1],
                                parts[2],
                                Difficulty.valueOf(parts[3]),
                                WordLength.valueOf(parts[4]),
                                parts[5]
                        ))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                System.err.println("Error loading game logs: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }

    // --- Hardest Words Persistence ---
    // Note: HardWordEntry is currently an inner record of HardestWordsPanel.
    // For proper persistence, it should ideally be a top-level record or in model.Records.
    public void saveHardestWords(List<HardWordEntry> hardWords) {
        try (BufferedWriter writer = Files.newBufferedWriter(dataDirectory.resolve(hardeestWordFile))) {
            for (HardWordEntry entry : hardWords) {
                writer.write(String.join(",",
                        String.valueOf(entry.rank()),
                        entry.word(),
                        String.valueOf(entry.hardnessScore())
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving hardest words: " + e.getMessage());
        }
    }

    public List<HardWordEntry> loadHardestWords() {
        Path hardWordsPath = dataDirectory.resolve(hardeestWordFile);
        if (Files.exists(hardWordsPath)) {
            try (BufferedReader reader = Files.newBufferedReader(hardWordsPath)) {
                return reader.lines()
                        .map(line -> line.split(",", -1))
                        .filter(parts -> parts.length == 3)
                        .map(parts -> new HardWordEntry(
                                Integer.parseInt(parts[0]),
                                parts[1],
                                Double.parseDouble(parts[2])
                        ))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                System.err.println("Error loading hardest words: " + e.getMessage());
            }
        }
        return new ArrayList<>();
    }
}
