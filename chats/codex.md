Converted ResourceLoaderTestCase to JUnit 5 (already done) and added a fast, injection-based timer test:
TimerController now has a package-private constructor that accepts an executor and tick interval; tests can run with 10ms ticks.
Added TimerControllerTestCase.java using the injected 10ms interval and short latches; the old flaky TurnTimerTestCase remains deleted.
Added a test hook to KeyboardPanel (debugButtons()) and updated KeyboardPanelTestCase to drop reflection.
Expanded GameUiModelMapperTestCase with a timed-game assertion: timed configs expose remaining seconds and duration, untimed configs keep them null.
Status of recommendations:

Timer test is less flaky via injectable tick interval; if you want it fully deterministic, we can add a fake scheduler/clock later.
Invariant tests (awaitingWinnerKnowledge disables input, untimed ticks ignored in UI, event-driven winner knowledge) are still outstanding; I haven’t added them yet.
GameUiModelMapper tests now cover timed/untimed remaining time; we can add more (finish reasons, keyboard mapping per difficulty) if you want.



Timer test hardened: TimerControllerTestCase.java now wraps start/await logic in try/finally (always stops/shuts down), and awaits were bumped to 2s to reduce flake risk.
Keyboard test strengthened: KeyboardPanelTestCase.java now asserts both disable and re-enable propagation (catches one-way disable bugs).