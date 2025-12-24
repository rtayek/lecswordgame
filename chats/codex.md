Converted ResourceLoaderTestCase.java to a proper JUnit 5 test with assertions (no more main/AssertionError smoke test).
Made TimerController tick interval injectable (keeps 1s default) and updated it to use the configurable interval; added a fast, less-flaky TimerControllerTestCase.java that counts down with a 10ms tick and asserts updates/expiry. Old flaky TurnTimerTestCase remains deleted.
Added a package-private test hook in KeyboardPanel.java to expose buttons; KeyboardPanelTestCase.java now uses it (no reflection).
Notes/next steps:

Please run your test suite to verify everything is green (TimerController now has an extra constructor for tests; default behavior unchanged).
If you want the invariant tests we discussed (awaitingWinnerKnowledge disables input, untimed ticks ignored, etc.), I can add those next.