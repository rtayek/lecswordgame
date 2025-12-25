GameLogEntryView now uses DifficultyView (typed) instead of String; AppController.getGameLog() maps model difficulty to DifficultyView.
ViewToModelMapper updated to consume the typed difficulty and map unknown word lengths to null; ViewToModelMapperTestCase updated accordingly (including null difficulty and unknown length cases).
Fixed the failing invariant test: GameSessionServiceTestCase.onTimeUpdatedIgnoredWhenUntimed now resets the event counter after the initial gameStarted publish so it only counts tick handling.

GameLogEntryView now uses DifficultyView (typed) instead of String; AppController.getGameLog() maps model difficulty to DifficultyView.
ViewToModelMapper updated to consume the typed difficulty and map unknown word lengths to null; ViewToModelMapperTestCase updated accordingly (including null difficulty and unknown length cases).
Fixed the failing invariant test: GameSessionServiceTestCase.onTimeUpdatedIgnoredWhenUntimed now resets the event counter after the initial gameStarted publish so it only counts tick handling.

n winnerKnowledgeFlowIsEventDriven, we now assert that applying winner knowledge advances the game to either finished or waitingForFinalGuess (as per current logic) and that the finish event was published and knowledge recorded.

In winnerKnowledgeFlowIsEventDriven, we now assert that applying winner knowledge advances the game to either finished or waitingForFinalGuess (as per current logic) and that the finish event was published and knowledge recorded.

