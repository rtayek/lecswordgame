mplemented explicit finish-reason support and mapping cleanup:

Added FinishReasonView enum and included finishReason in GameUiModel.
GameUiModelMapper now accepts an optional finish reason; GameSessionService publishes it (guessed on guess/knowledge completion, timeout on expiration).
GameSessionServiceTestCase now asserts timeout finish reason and adjusts event counting.
Mapping utility renamed behavior: ViewToModelMapper gained toView(Difficulty) earlier and now has a helper for word-length mapping.
Cleaned AppController to use mapper for difficulty mapping and removed unused import.