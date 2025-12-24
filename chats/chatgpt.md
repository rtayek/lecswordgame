Here’s where you are in the **current tar** (I unpacked and inspected it), and what we were doing.

## Where we are right now

### The architecture tightenings are essentially **done**

This tar has the “end state” we were driving toward:

* **UI is intent-only + event-driven**
  `BaseGamePanel.handleGuess()` submits intent + clears; UI updates come from events.

* **Rendering is snapshot-driven (DTO-only views)**
  `src/view` has **no `import model.*`**. Guess rows, keyboard state, status, etc. are rendered from `GameUiModel` / `KeyboardView` / `GuessView`.

* **Events are clean and split**
  `GameEventListener` is now two callbacks:

  * `onGameStateEvent(kind, GameUiModel)`
  * `onTimerEvent(TimerView)`

* **Timers no longer leak domain types**
  Timer identity uses `PlayerSlot` end-to-end.

* **EDT safety is handled**
  `BaseGamePanel` wraps both state events and timer events using `SwingUtilities.invokeLater(...)` when needed.

* **The concrete bug fixes you asked for are present**

  * `KeyboardPanel.setEnabled(...)` propagates to child buttons.
  * `SoloGamePanel.onGameFinished(...)` disables `submitButton`.
  * `timerExpired` is gone (no lingering references).
  * Timer ticks are ignored for untimed games (`timerDurationSeconds() == 0`).

So: you are not in the middle of a refactor anymore. You are at a stable baseline.

## What we were doing

We were eliminating “split-brain UI” problems:

* domain objects sneaking into Swing
* UI deciding outcomes from return values instead of events
* timer plumbing leaking model identity
* UI enable/disable not actually disabling input in Swing
* redundant event kinds (`timerExpired`)
* EDT correctness

That work is now complete in this tar.

## Recommendations from here (finite and practical)

### 1) Freeze architecture and switch to features

You will absolutely end up in loops again if you keep “improving architecture.” You’ve reached the good plateau. Next work should be feature/UX:

* hardest/best words list (data capture + ranking)
* game log screen completeness
* friend add/search flows
* polish outcome prompts and sound/graphics hooks

### 2) Small cleanup I *would* still do (optional)

These are low-risk, but not required:

* Remove/park `src/cli/CommandLineGame.java` if CLI is truly low priority (it’s harmless, but it’s noise).
* Add 2–3 tests that lock in the regressions you just fixed:

  * “KeyboardPanel.setEnabled(false) disables all buttons”
  * “Untimed game ignores timer ticks”
  * “Solo finished disables submit”

### 3) One correctness watch item (don’t “fix” unless it bites you)

Timers are updated via both:

* initial render from snapshot (`updateTimersFromModel`)
* live ticks (`updateTimersFromTimer`)

This is fine as implemented because ticks are ignored when untimed and you aren’t overwriting ticks with stale snapshots, but keep it that way.

---

If you tell me what feature you want next (timers UX, hardest words, game log, or friends/profile), I’ll give you a concrete implementation plan that doesn’t reopen architecture.
