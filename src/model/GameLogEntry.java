package model;

import model.enums.Difficulty;
import model.enums.WordLength;

public record GameLogEntry(String gameId,
                           String playerOneName,
                           String playerTwoName,
                           Difficulty difficulty,
                           WordLength wordLength,
                           String resultSummary) { }
