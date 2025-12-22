I unpacked the **current tar** and did a focused architecture pass (controller/event boundaries, view imports, timer wiring, outcome flow, and the “DTO-only” claim).

## What’s already in a good place (do not churn)

* **View layer is clean of domain types**: `src/view` has **no `import model.*`**. Your gameplay UI really is DTO/snapshot-driven now.
* **Event boundary is sane**: `GameEventListener` receives `GameUiModel` and `TimerView`. No “metadata backdoor”.
* **Timer leak is fixed**: `TurnTimer` is now keyed by `PlayerSlot`, not `model.GamePlayer`.

This is the “boring, deterministic” core you were aiming for. Treat it as frozen.

---

## High-value recommendations (small, correctness-oriented)

### 1) Fix the winner-knowledge prompt flow (currently brittle)

In `MultiplayerGamePanel.onGameFinished(...)`, when `NextAction.ASK_WINNER_KNOWLEDGE`:

* You prompt
* Call `appController.reportWinnerKnowledge(...)`
* Then immediately do `outcomePresenter.build(uiModel)` again using the *old* snapshot

That’s logically wrong: after reporting knowledge, the authoritative state is the **new snapshot** produced by the controller events, not the one you already have.

**Recommendation**
Make `onGameFinished` strictly event-driven:

* If `ASK_WINNER_KNOWLEDGE`: show dialog, call `reportWinnerKnowledge`, then **return**
* Let the subsequent `gameStateUpdated/gameFinished` event drive the next outcome rendering

This removes a race/consistency hazard and makes your “events are authoritative” rule real.

---

### 2) Rename or split `gameFinished` (semantic clarity)

Right now `GameEventKind.gameFinished` is used for:

* truly finished games (`status == finished`)
* *also* “stop gameplay and ask something” (`awaitingWinnerKnowledge`, last chance prompt)

That’s fine mechanically, but semantically confusing and encourages UI-side inference.

**Recommendation (minimal)**
Keep `gameFinished`, but add a second kind such as:

* `uiInterruption` (for last-chance/knowledge prompt), or
* `outcomeAvailable`

…and only use `gameFinished` when `status == finished` (or `soloChase` if you treat that as terminal UI state).

This is low risk and reduces future “why did gameFinished fire here?” confusion.

---

### 3) Remove redundant “mapDifficulty” plumbing in `BaseGamePanel`

`BaseGamePanel.mapDifficulty(DifficultyView value)` returns the same type with a null default; it’s dead weight.

**Recommendation**
Inline the null default where used, delete the method.

---

### 4) Tighten the “timer tick” update model (optional, but simplifies UI)

You currently publish timer ticks via `onTimerEvent(TimerView)` **and** also compute remaining time inside `GameUiModelMapper` via `turnTimer.getRemainingFor(...)`.

This duplication is not wrong, but it gives you two ways to render time and can drift.

**Recommendation**
Pick one:

* **Option A (simplest UI):** only use `TimerView` tick events for updating labels; `GameUiModel` includes the initial duration and maybe last-known remaining
* **Option B (pure snapshot):** remove `TimerView` events and publish only `gameStateUpdated` snapshots on ticks (may be heavier)

Given Swing and small UI: Option A is fine; just avoid mixing both pathways in the same screen.

---

## Medium-value recommendations (cleanup, not urgent)

### 5) Remove unused parameters and tighten signatures

`AppController.startGame(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, ...)` takes `playerOne/playerTwo` but doesn’t use them.

**Recommendation**
Drop unused params. This reduces “phantom dependencies”.

---

### 6) DTO-ize the remaining “service screens” (only if they start to hurt)

Your gameplay panels are DTO-only; some non-game flows still accept model types via the controller surface (e.g., `addGameLogEntry(model.GameLogEntry)`).

Not a correctness issue, but if you want a consistent rule:

* expose `GameLogEntryView` inputs/outputs only, and map inside controller/service

---

## One thing to *not* do

Do **not** let anyone refactor `GameState` into a formal State pattern. Your current state machine is already testable and constrained; that refactor is high-risk for near-zero payoff.

---

## If you want a single next task that pays off immediately

**Fix the winner-knowledge prompt flow** (#1). It’s the only item above that can plausibly bite you as a real bug under timing/event ordering, and it’s a small change.

If you tell me whether you want the prompt-handling to be driven by a dedicated event kind (recommended) or just by `status` in the snapshot, I’ll give you a precise, file-by-file edit plan.
