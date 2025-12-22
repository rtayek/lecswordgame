package controller;

import controller.events.PlayerSlot;

/**
 * Domain-facing timer abstraction for per-turn countdowns.
 */
public interface TurnTimer {
    interface Listener {
        void onTimeUpdated(PlayerSlot slot, int remainingSeconds);
        void onTimeExpired(PlayerSlot slot);
    }

    void addListener(Listener listener);
    void removeListener(Listener listener);

    void setTimeForPlayer(PlayerSlot slot, int seconds);
    int getRemainingFor(PlayerSlot slot);

    void start(PlayerSlot slot);
    void stop();
    void reset();
}
