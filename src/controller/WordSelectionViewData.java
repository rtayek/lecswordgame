package controller;

public record WordSelectionViewData(
        String opponentName,
        int wordLength,
        boolean multiplayer,
        boolean isPlayerOneTurn
) { }
