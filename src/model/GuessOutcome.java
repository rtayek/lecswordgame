package model;

import model.enums.GameStatus;

public record GuessOutcome(GuessEntry entry, GameStatus status, GamePlayer nextTurn) { }
