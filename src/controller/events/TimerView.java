package controller.events;

/**
 * Lightweight timer payload for UI updates.
 */
public record TimerView(PlayerSlot slot, int remainingSeconds) { }
