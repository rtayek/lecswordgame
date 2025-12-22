Applied the requested architecture refactors:

Removed module-info.java to keep tests on the classpath.
Added GameUiModelMapper and KeyboardViewBuilder to cleanly map domain state and keyboard data; GameSessionService now uses them and no longer builds UI snapshots inline.
Typed KeyboardView to Map<Character, LetterFeedbackView> and updated KeyboardPanel to consume typed states.
Added timerUpdated event; GameSessionService publishes it on ticks and views handle it via existing update logic.
Eliminated timer exposure to the view layer: dropped getTimerController from Navigation/MainFrame, and AppController no longer exposes the timer.
JUnit tests were already converted; controller tests remain under *TestCase.java.
Please rebuild to ensure everything compiles with the new helpers and event type.