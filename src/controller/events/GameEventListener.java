package controller.events;

public interface GameEventListener {
    void onGameStateEvent(GameEventKind kind, GameUiModel view);
    void onTimerEvent(TimerView timer);
}
