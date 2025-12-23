I unpacked the **fresh tar** and reviewed the current architecture and a few “this will bite you later” correctness edges. Overall: the big refactors are holding, and the code is in a good place. The remaining recommendations are small, mechanical, and high-ROI.

## What’s solid and should stay frozen

* Gameplay rendering is **snapshot/DTO driven** (`GameUiModel`, typed enums, typed `KeyboardView`).
* Input is **intent-only** (`handleGuess()` submits + clears; events drive rendering).
* Listener split (`onGameStateEvent` vs `onTimerEvent`) is clean.
* Timer identity is **DTO-safe** (`PlayerSlot`, not `model.GamePlayer`).
* You’re now EDT-safe in the base listener path (wrapping with `SwingUtilities.invokeLater`).

## Top recommendations

### 1) Fix `KeyboardPanel.setEnabled(false)` not actually disabling the keys (real bug)

Right now, panels do things like `keyboardPanel.setEnabled(false)` and assume keys stop working. In Swing, disabling a container **does not disable its children** automatically. Your buttons still fire, and they still append to the (disabled) `guessField` because your callback calls `guessField.setText(...)` regardless of enabled state.

**Recommendation**
Override `setEnabled(boolean)` in `KeyboardPanel` and propagate to all buttons (and optionally ignore clicks when disabled). This is the single most important UI correctness fix left.

Minimal shape:

```java
@Override
public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    for (JButton b : buttons.values()) {
        b.setEnabled(enabled);
    }
}
```

### 2) Solo game: disable the submit button when finished

In `SoloGamePanel.onGameFinished(...)` you disable `guessField` and `keyboardPanel`, but **not** `submitButton`. That means “Submit” can still be clicked, calling `handleGuess()` and creating noise.

**Recommendation**
Add `submitButton.setEnabled(false);` in `SoloGamePanel.onGameFinished(...)`.

### 3) `timerExpired` should not be treated as `gameStateUpdated`

`GameSessionService` publishes `timerExpired` and then `gameFinished`. In `BaseGamePanel`, you currently route `timerExpired -> onGameStateUpdated(view)`, which is semantically odd and can cause redundant rendering.

**Recommendation**
Either:

* handle `timerExpired` as a no-op (or a status message only), letting `gameFinished` drive the end dialog, **or**
* remove `timerExpired` entirely and rely on `publishTimer(...0)` + `gameFinished`.

The lowest-churn change: make `timerExpired` a no-op in the switch (with a comment).

## Medium priority (only if you want to polish)

* Confirm you’re not updating timer labels from both snapshots and ticks in competing ways. Initial render from snapshot is fine; after that, let tick events be the only updater.

## Bottom line

You’re past architecture churn. The main remaining issues are **UI enable/disable correctness**, especially the keyboard container problem. If you do only one thing, fix `KeyboardPanel.setEnabled(...)` propagation.
