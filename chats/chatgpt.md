I unpacked and reviewed the **fresh tar**. Overall: this is a solid “tightened” iteration. The core architectural loop (intent in, snapshots/events out, no domain leaking into views) is now genuinely closed.

## What’s clearly improved and should be treated as frozen

* **No `model.*` imports in `src/view`** (DTO/snapshot boundary holds).
* **Listener API is clean**: `GameEventListener` is split into:

  * `onGameStateEvent(GameEventKind, GameUiModel)`
  * `onTimerEvent(TimerView)`
* **Winner-knowledge flow is correct**: prompt → `reportWinnerKnowledge(...)` → **return**, wait for next event.
* **Timer identity leak is gone**: timers use `PlayerSlot` end-to-end.
* **Untimed games no longer show “00:00”** (Multiplayer/Solo now blank timers when duration is none).
* **Keyboard styling fixed**: unused letters are black on default background; used letters are colored with white text.

These are exactly the right “tightenings.”

---

## Recommendations that are still worth doing

### 1) SoloGamePanel: disable `submitButton` when finished

In `SoloGamePanel.onGameFinished(...)` you disable `guessField` and `keyboardPanel`, but **not** `submitButton`. Clicking it can still call `handleGuess()` and produce noisy errors.

**Fix**
In `onGameFinished` add:

* `submitButton.setEnabled(false);`

(You already do this correctly in Multiplayer via status-based logic.)

---

### 2) `timerExpired` handling: stop using it as “gameStateUpdated”

In `BaseGamePanel.onGameStateEvent(...)`, you currently route:

```java
case timerExpired -> onGameStateUpdated(view);
```

But `GameSessionService` already publishes:

* `publishTimer(slot, 0)`
* `publishState(timerExpired)`
* `publishState(gameFinished)`

So mapping `timerExpired` to “state updated” is semantically odd and can cause duplicate UI work.

**Two clean options**

* **Option A (minimal):** handle `timerExpired` explicitly as a no-op (or status message only), and let `gameFinished` drive the end dialog.
* **Option B:** remove the `timerExpired` kind entirely and rely on `publishTimer(...0)` + `gameFinished`.

I’d do **Option A** now (lowest churn, most clarity).

---

### 3) Make `addGameLogEntry` DTO-only (small boundary cleanup)

`AppController.addGameLogEntry(model.GameLogEntry entry)` still exposes a domain type on the public surface. It’s not dangerous, but it’s inconsistent with your “view talks DTOs” rule.

**Fix**

* Change signature to `addGameLogEntry(GameLogEntryView entry)`
* Map inside AppController/ProfileService

Do this only if you expect to expand logs/friends/screens; otherwise it’s optional.

---

### 4) Timer + snapshot: keep the ownership rule explicit

Right now you have both:

* initial timer display from `GameUiModel` (good)
* tick updates from `TimerView` (good)

That’s fine. Just keep one rule:

> After first paint, **only TimerView ticks** should update timer labels.

Avoid also rewriting timer labels in response to every `gameStateUpdated`, or you’ll eventually see flicker/stale overwrites.

(Your panels are already close to this; this is mostly a “don’t regress” warning.)

---

## One-liner next commit suggestion

If you want a single small, high-confidence commit:

1. Disable `submitButton` in `SoloGamePanel.onGameFinished`
2. Stop routing `timerExpired` → `onGameStateUpdated` (either no-op or remove the kind)

Everything else can wait.

If you want, I can point to the exact lines for the `timerExpired` adjustment and propose the cleanest “Option A” switch block change.
