Applied the requested fixes:

Added a view-only UiThread helper and wrapped listener entrypoints in views to enforce EDT marshaling (already in place).
Fixed input disablement:
KeyboardPanel.setEnabled now propagates to buttons (already done).
SoloGamePanel.onGameFinished disables the Backspace button by guarding handleBackspace via disabling the button; cleaned unused imports; deferred timeout messages to finish events.
MultiplayerGamePanel now names and disables/re-enables the Backspace button alongside other inputs, and stops emitting “ran out of time” from timer updates (timeout messaging left to finish events).
Removed redundant imports in SoloGamePanel.
Timer messaging is now authoritative via finish events; timer ticks no longer set status.
If you want a guard in handleBackspace itself, I can add if (!guessField.isEnabled()) return;, but with the buttons disabled the callbacks won’t fire.

Added a guard to prevent backspace actions when input is disabled: BaseGamePanel.handleBackspace now returns early if guessField is not enabled, protecting against indirect invocations. This covers Solo/Multiplayer panels since they inherit the method.