package model.enums;

public enum TimerDuration {
    none(0), oneMinute(60), threeMinutes(180), fourMinutes(240), fiveMinutes(300);
    TimerDuration(int seconds) {
        this.seconds = seconds;
    }
    public int seconds() {
        return seconds;
    }
    public boolean isTimed() {
        return seconds > 0;
    }
    final int seconds;
}
