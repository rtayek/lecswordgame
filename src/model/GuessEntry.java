package model;

public record GuessEntry(GamePlayer player, GuessResult result, long timestampMillis) { }
