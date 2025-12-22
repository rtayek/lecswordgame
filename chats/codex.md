Implemented the requested tightenings:

Event clarity: Replaced the nullable union with split callbacks—GameEventListener now has separate onGameStateEvent and onTimerEvent. GameEventKind stands alone; state events carry GameUiModel, timer events carry TimerView.
Winner-knowledge flow fixed: MultiplayerGamePanel now prompts, calls reportWinnerKnowledge, and returns—waiting for the next event to render the outcome (no reuse of stale snapshots).
Keyboard typing retained: KeyboardView remains strongly typed with LetterFeedbackView.
Controller surface tightened: AppController no longer exposes the timer, and the internal startGame no longer takes unused player arguments.
Hygiene: Removed unused GameMode import; inlined the null-default difficulty mapping in BaseGamePanel; deleted the unused mapDifficulty helper.
Notes:

If you want to fully enforce the DTO-only boundary, we can introduce a UI-facing start-game request DTO and keep GameConfig internal, but requestNewGame/buildWordSelectionData are already private.
Timer rendering currently uses both snapshot (initial render) and timer events (ticks); panels now handle timers via the dedicated timer callback.
Please rebuild to pick up the new listener signatures and panel changes.