package model;

import model.Enums.Difficulty;
import model.Enums.WordLength;

public record GameLogEntry(String gameId,
                           String playerOneName,
                           String playerTwoName,
                           Difficulty difficulty,
                           WordLength wordLength,
                           String resultSummary) { }
