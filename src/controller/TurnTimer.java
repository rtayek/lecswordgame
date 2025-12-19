package controller;

import model.GamePlayer;

/**
 * Domain-facing timer abstraction for per-turn countdowns.
 */
public interface TurnTimer {
    interface Listener {
        void onTimeUpdated(GamePlayer player, int remainingSeconds);
        void onTimeExpired(GamePlayer player);
    }

    void addListener(Listener listener);
    void removeListener(Listener listener);

    void setTimeForPlayer(GamePlayer player, int seconds);
    int getRemainingFor(GamePlayer player);

    void start(GamePlayer player);
    void stop();
    void reset();
}
