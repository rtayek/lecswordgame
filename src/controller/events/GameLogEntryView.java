package controller.events;

public record GameLogEntryView(
        String playerOneName,
        String playerTwoName,
        String difficulty,
        int wordLength,
        String resultSummary) { }
