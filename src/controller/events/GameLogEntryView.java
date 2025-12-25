package controller.events;

public record GameLogEntryView(
        String playerOneName,
        String playerTwoName,
        DifficultyView difficulty,
        int wordLength,
        String resultSummary) { }
