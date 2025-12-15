package model;

public class GuessEntry {

    public GuessEntry(GamePlayer player, GuessResult result, long timestampMillis) {
        this.player = player;
        this.result = result;
        this.timestampMillis = timestampMillis;
    }

    public GamePlayer getPlayer() {
        return player;
    }

    public GuessResult getResult() {
        return result;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    final GamePlayer player;
    final GuessResult result;
    final long timestampMillis;
}
