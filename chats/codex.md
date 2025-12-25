Implemented the “mapper injection” tweak to keep GameSessionService from acting as a composition root:

GameSessionService now takes a GameUiModelMapper in its constructor (no longer constructs one internally).
AppController constructs and injects the mapper (new GameUiModelMapper(turnTimer, new KeyboardViewBuilder())).
All test sites updated to pass a mapper (using the test timers) instead of relying on internal creation.
Also made the tiny backspace guard tweak: BaseGamePanel.handleBackspace() now checks isEnabled() before reading text.

If you want, I can also rename ViewModelMapper tests/files to match the new naming (already done) and run the test suite.