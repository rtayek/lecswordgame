package controller.events;

/**
 * View-friendly timer durations.
 */
public enum TimerDurationView {
    none(0),
    oneMinute(60),
    threeMinutes(180),
    fourMinutes(240),
    fiveMinutes(300);

    private final int seconds;

    TimerDurationView(int seconds) {
        this.seconds = seconds;
    }

    public int seconds() {
        return seconds;
    }

    public boolean isTimed() {
        return seconds > 0;
    }
}
