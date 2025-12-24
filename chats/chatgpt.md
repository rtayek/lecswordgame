I pulled the **current tar** and reviewed the unit tests under `tst/**`. Here’s what your evil twin actually did, and what I recommend next.

## What he did (confirmed in this tar)

### 1) Converted the old “main-method smoke test” into JUnit 5

* `tst/util/ResourceLoaderTestCase.java` is now a proper JUnit 5 test.
* It loads `app.png` from the classpath and exercises `ResourceLoader.getScaledImage(...)`.

This is good: it now runs in your normal test workflow.

### 2) Added a regression test for the Swing “disable container doesn’t disable children” pitfall

* `tst/view/KeyboardPanelTestCase.java` verifies `KeyboardPanel.setEnabled(false)` disables all key buttons.
* To support that, `KeyboardPanel` now exposes a **package-private** `debugButtons()` accessor returning an unmodifiable collection.

This is an excellent regression test and the accessor is appropriately scoped.

### 3) Added a short-interval TimerController test

* `tst/controller/TimerControllerTestCase.java` uses an injected scheduler and a **10ms tick interval** and asserts that:

  * time updates happen, and
  * expiry callback happens.
* It has `@Timeout(5)` to prevent hangs.

This is “deterministic-ish” and acceptable, though it’s the test most likely to flake on a very slow machine.

### 4) Existing controller/service tests look reasonable

* `GameSessionServiceTestCase` uses a `NoopTimer` and asserts finish states / timer stop behavior.
* `GameUiModelMapperTestCase` covers snapshot mapping behavior (good place to expand).
* `GameControllerTestCase` is thin but sane.

## Recommendations (high ROI)

### A) Make the timer test truly deterministic (optional, but best long-term)

Right now `TimerControllerTestCase` still relies on real scheduling + latches. It’s not terrible, but it’s the most likely future flake.

Best upgrade: make `TimerController` accept a tiny “scheduler/clock” abstraction and test with a fake scheduler you manually advance.

If you don’t want that now: keep it, but consider increasing headroom slightly (e.g., await 2 seconds instead of 1) if it ever flakes.

### B) Add 2–3 invariants tests that lock in your architecture

These are the tests that prevent you from sliding back into “UI decides state”:

1. **Untimed ignores ticks**: calling `onTimerEvent` should not change timer labels when duration is none.
2. **No guessing while awaiting winner knowledge**: submitGuess is rejected and view disables input.
3. **Winner-knowledge flow is event-driven**: prompt triggers intent; next state arrives via event (no stale snapshot reuse).

### C) Expand `GameUiModelMapperTestCase` first

That mapper is your DTO boundary. It’s the cleanest place to test:

* finish reason mapping
* status transitions reflected in DTO
* keyboard feedback mapping per difficulty mode

## Bottom line

Your evil twin’s test work is net-positive:

* JUnit 5 conversion: good
* Keyboard disable regression test: very good
* Timer test: acceptable (watch for flakiness)
* Controller/service tests: fine but should grow around invariants

If you want, paste the output of `find tst -name "*.java" -maxdepth 3 -print` (or just the test filenames), and I’ll propose a tight “next 5 tests” list that maximizes architectural protection without testing Swing internals.
