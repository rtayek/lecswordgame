I unpacked the **current tar** and did a targeted architecture + correctness review (controller/event boundaries, UI snapshot integrity, timer plumbing, and outcome flow). You are **not** in an architecture crisis; you have a handful of **real, actionable fixes**.

## The 3 most important issues

### 1) Bug: Untimed games can look like “00:00” and trigger “ran out of time”

`GameUiModelMapper` currently sets `playerOneRemaining/playerTwoRemaining` whenever the player exists, regardless of whether the game is timed:

```java
Integer p1Remaining = turnTimer != null && config.playerOne() != null
    ? turnTimer.getRemainingFor(PlayerSlot.playerOne)
    : null;
```

For **untimed** games you call `turnTimer.reset()`, and `getRemainingFor()` returns `0`, so your UI can display **00:00** and even show “ran out of time!” (depending on panel logic).

**Recommendation**
Only populate remaining time when `config.timerDuration().isTimed()` is true:

* If untimed: set remaining to `null` (so views fall back to “no timer” behavior)
* If timed: use the timer values

This is the most important correctness fix in the tar.

---

### 2) KeyboardPanel styling bug: unused letters can become white on default background

In `KeyboardPanel.applyStyles(...)` you always do:

```java
button.setForeground(Color.WHITE);
```

Even for unused keys where background is null (LAF-dependent), this can make unused letters hard to read.

**Recommendation**
Set foreground based on whether the key is unused:

* `unused` → default background, `opaque=false`, `foreground=BLACK`
* used → colored background, `opaque=true`, `foreground=WHITE`

Small change, immediate UX improvement.

---

### 3) Outcome prompting logic can ask “Did you know the word?” in cases it shouldn’t

`GameOutcomePresenter` asks winner knowledge in `finished` whenever `winnerKnewWord == null`:

```java
if (winnerKnewWord == null) { ASK_WINNER_KNOWLEDGE }
```

But `GameState.handleTimeout(...)` can finish a game without any “knew word” concept, leaving `winnerKnewWord` null. That can lead to nonsensical prompting after a timeout.

**Recommendation**
You need an explicit “why did the game finish?” signal available to the presenter. Minimal options:

* Add to `GameUiModel`: `FinishReasonView { guessed, timeout, giveUp, … }`
* Or add per-player finish states (`finishedSuccess/finishedFail/notFinished`) to the UI model and infer “timeout”/forfeit

Then: only prompt winner knowledge when the finish reason is “guessed”.

## Medium-value cleanup

### 4) Remove unused/ambiguous event kind

`GameEventKind.timerUpdated` exists but is not used in the `onGameStateEvent` switch; timer ticks come through `onTimerEvent(TimerView)`.

**Recommendation**
Either:

* remove `timerUpdated`, or
* actually publish it and handle it (but you already have `onTimerEvent`, so removing is cleaner).

### 5) Remove redundant null checks in panels

`MultiplayerGamePanel.onGameFinished()` does `if (vm == null) return;` and later repeats `if (vm == null) return;`.

**Recommendation**
Delete the second check. Same class of cleanup exists in a couple places.

### 6) Disable input when status is `awaitingWinnerKnowledge`

In multiplayer, the UI can remain enabled based on “currentPlayer != finished”, but the controller rejects guesses in `awaitingWinnerKnowledge`.

**Recommendation**
In `updateCurrentPlayerLabelFromModel()` (or a centralized “setInputEnabled” helper), disable input when:

* `status == awaitingWinnerKnowledge`
  Enable when:
* `inProgress`, `waitingForFinalGuess`, `soloChase`

## What I would not refactor right now

* No State-pattern rewrite of `GameState`
* No additional splitting of `GameSessionService` unless you’re adding persistence/networking
* No more DTO boundary churn in gameplay UI; that part is already working

## Suggested next commit (highest ROI)

1. Fix `GameUiModelMapper` to only expose remaining time for timed games
2. Fix `KeyboardPanel` foreground/background logic
3. Add `finishReason` (or finish-state fields) to `GameUiModel` and update `GameOutcomePresenter` to avoid knowledge prompts on timeout

If you want, I can point to the exact lines/files to change for (1) and (2) and the smallest schema change for (3) that won’t cascade through your code.
