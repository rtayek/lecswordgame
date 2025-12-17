package model;

import java.util.List;
import model.Enums.Difficulty;
import model.Enums.LetterFeedback;
import model.Enums.WordLength;
import model.Enums.WordSource;

public final class Records {
    private Records() { }

    public static record GameLogEntry(String gameId,
                                      String playerOneName,
                                      String playerTwoName,
                                      Difficulty difficulty,
                                      WordLength wordLength,
                                      String resultSummary) { }

    public static record PlayerProfile(String username, String avatarPath) {
        @Override
        public String toString() {
            return "%s (%s)".formatted(username, avatarPath);
        }
    }

    public static record GamePlayer(PlayerProfile profile, boolean human) {
        @Override
        public String toString() {
            var name = profile != null ? profile.username() : "Unknown";
            return (human ? "Human" : "AI") + ": " + name;
        }
    }

    public static record WordChoice(String word, WordSource source) { }

    public static record GuessResult(String guess,
                                     List<LetterFeedback> feedback,
                                     int correctLetterCount,
                                     boolean exactMatch) {

        public GuessResult {
            feedback = feedback == null ? List.of() : List.copyOf(feedback);
        }

        @Override
        public List<LetterFeedback> feedback() {
            return List.copyOf(feedback);
        }
    }

    public static record GuessEntry(GamePlayer player, GuessResult result, long timestampMillis) { }

    public static record HardWordEntry(int rank, String word, double hardnessScore) { }
}
