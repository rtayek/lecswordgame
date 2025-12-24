What the tests now do well
1. Keyboard disable regression is nailed

KeyboardPanelTestCase is exactly the kind of test you want at this stage.

Verifies setEnabled(false) disables all key buttons

Uses a package-private accessor, not reflection

Protects against a very common Swing regression

✅ Keep this test.

2. DTO / snapshot boundary is protected

GameUiModelMapperTestCase asserts:

Untimed games produce null remaining time

Timed games produce real values

This is excellent. This class is your DTO contract wall.

✅ This is the right place to add more tests later.

3. Controller and session tests are behavioral, not structural

GameControllerTestCase and GameSessionServiceTestCase:

Assert outcomes (“still in progress”, “winner chosen”)

Do not assert internal field mutations

Do not inspect Swing or DTO internals

✅ This keeps refactoring safe.

The one real risk: timer test flakiness
TimerControllerTestCase

This test:

Uses a real scheduled executor

Uses a short (≈10ms) tick interval

Uses latches + timeouts

It’s acceptable, but it is the most likely test to flake on:

slow CI

loaded machines

JVMs under heavy GC

What I recommend (minimum change)

If nothing is failing right now, do this small hardening:

Wrap setup in try/finally to guarantee cleanup

Increase await window slightly (e.g., 2s instead of 1s)

That’s enough to reduce intermittent failures by an order of magnitude.

I would not redesign timers or add fake schedulers unless flakes appear.

What’s missing (but worth adding)

These are architectural invariant tests—they prevent you from ever re-entering the refactor loop.

High-value missing tests (pick 2–3)

Untimed ignores timer ticks

Call onTimerEvent(...) when timerDurationSeconds() == 0

Assert UI state does not change

No guesses accepted during awaitingWinnerKnowledge

Submit guess during that state

Assert state unchanged / no new guess added

Winner-knowledge flow is event-driven

Trigger knowledge prompt

Call reportWinnerKnowledge

Assert next state arrives via event, not by reusing old snapshot

These tests protect the decisions you just finished making.

Tests I would not add

Swing layout tests

Pixel / color assertions

“Exact sequence of events” tests

Reflection-based assertions on private state

Those increase maintenance cost without protecting architecture.

Bottom line

You are in a good place.

✅ Tests reinforce your architecture instead of fighting it

⚠️ One timer test should be slightly hardened

➕ A few invariant tests would lock in the design permanently