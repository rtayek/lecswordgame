AppController.addGameLogEntry now accepts GameLogEntryView (no model.* leak) and maps internally.
Introduced ViewToModelMapper.java to centralize all view→model conversions (difficulty, word length, timer duration, word choice, game log).
AppController uses the mapper for all conversions; redundant private mapping methods were removed.
If you want, I can add unit tests for ViewToModelMapper next to lock in the mapping rules.

Added ViewToModelMapperTestCase.java to lock in mapping rules for difficulty, word length, timer duration, word choice, and game log entries (including defaults and unknown difficulty fallback).
Introduced ViewToModelMapper.java and updated AppController to use it; addGameLogEntry now takes GameLogEntryView instead of model types.

Fixed the compilation errors by reintroducing GameConfig/WordChoice imports and using the GameConfig type consistently in AppController (requestNewGame, startGame, buildWordSelectionData, and game start methods).
Added mapping unit coverage: ViewToModelMapperTestCase covers difficulty/word length/timer defaults, word choice, game log mapping (including unknown difficulty fallback).
Introduced ViewToModelMapper and updated AppController to use it; addGameLogEntry now accepts GameLogEntryView.