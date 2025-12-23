I unpacked the **current tar** and validated the “evil twin” claims against the actual code. Net: the refactor is mostly *good and coherent*, and you are no longer fighting DTO boundaries. The remaining work is now small, correctness-oriented, and (importantly) finite.

## What’s confirmed as done and correct

* **Split listener callbacks**: `GameEventListener` is now two methods:
  `onGameStateEvent(GameEventKind, GameUiModel)` and `onTimerEvent(TimerView)`.
  No nullable unions / metadata backdoors.
* **Timer identity leak is fixed**: `TurnTimer.Listener` uses `PlayerSlot`, not `model.GamePlayer`.
* **Winner-knowledge flow is now event-correct**: Multiplayer and Solo prompt → `reportWinnerKnowledge()` → **return**, waiting for the next event to render outcome (no stale snapshot reuse).
* **Keyboard is strongly typed**: `KeyboardView` carries `LetterFeedbackView`, and `KeyboardPanel` uses typed feedback.
* **Untimed snapshots are fixed**: `GameUiModelMapper` only fills `playerOneRemaining/playerTwoRemaining` when `timerDuration.isTimed()`.

These are the right tightenings.

## The top recommendations from this tar

### 1) Fix Swing threading for events and timer ticks (high risk if ignored)

`GameSessionService` calls listeners directly from whatever thread the timer uses. `BaseGamePanel` updates Swing components directly in `onGameStateEvent` and `onTimerEvent`.

**Recommendation:** Guarantee EDT delivery. Pick one approach:

* Wrap calls in the view: in `BaseGamePanel.onGameStateEvent` and `onTimerEvent`, do `SwingUtilities.invokeLater(...)` before touching Swing; or
* Wrap publishing in `GameSessionService` (publish on EDT).

If you don’t do this, you will eventually get random UI glitches/hangs that look “haunted.”

### 2) Disable input during `awaitingWinnerKnowledge` (user-facing bug)

In `MultiplayerGamePanel.updateCurrentPlayerLabelFromModel()`, input is enabled for all non-`finished` statuses. But `GameSessionService.submitGuess()` rejects guesses during `awaitingWinnerKnowledge`.

**Recommendation:** In `updateCurrentPlayerLabelFromModel()`:

* if status == `awaitingWinnerKnowledge`: disable `submitButton`, `guessField`, `keyboardPanel`.

This avoids the “click submit → exception text in status bar” UX.

### 3) Stop showing “00:00” in untimed games (polish + prevents confusion)

Even though remaining seconds are `null` when untimed (good), the panels still initialize timer labels to `"00:00"` and update them using `timerDurationSeconds()` (which is 0 for untimed), so untimed games still look timed-but-empty.

**Recommendation:** In `updateTimersFromModel` (and `onGameStarted`) for Solo/Multiplayer:

* If `model.timerDurationSeconds() == 0`: set label to `""` (or `"--:--"`) and keep it black.

### 4) Either handle `timerExpired` or remove it (clarity)

`GameEventKind.timerExpired` is published, but `BaseGamePanel.onGameStateEvent` ignores it. That’s fine if timer expiration is fully handled by the timer ticks + `gameFinished`, but then the extra kind is misleading.

**Recommendation:** Either:

* Add a case for `timerExpired` (even if it’s a no-op with a comment), or
* Remove `timerExpired` and rely on the existing flow.

## Small cleanup (optional)

* In `SoloGamePanel.onGameFinished`, `toShow` is redundant; the null check after the `ASK_WINNER_KNOWLEDGE` return is dead code. Clean it up when convenient.
* `AppController.addGameLogEntry(model.GameLogEntry)` is still model-facing. Not urgent, but if you want a strict “view-only public surface,” convert that to `GameLogEntryView`.

## Bottom line

The refactor **improved the architecture** and did not introduce new boundary debt. Your next “best” changes are:

1. **EDT safety** for event/timer callbacks
2. **Disable input** while awaiting winner knowledge
3. **Hide 00:00** for untimed games

If you want one concrete next commit: do **(1)** first. It’s the only item that can cause intermittent, hard-to-debug failures later.
