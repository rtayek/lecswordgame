Implemented the remaining architectural tightenings:

Event payloads are no longer nullable unions: GameEventListener now has separate callbacks for state and timer events. GameEventKind stands alone; GameSessionService dispatches timerUpdated/state events directly without wrapping in nullable records, and BaseGamePanel handles them via dedicated methods.
Timer tick noise removed: onTimeUpdated now publishes only a minimal TimerView; full snapshots are sent only for game events.
Keyboard DTO fully typed: KeyboardView stays Map<Character, LetterFeedbackView> with KeyboardViewBuilder producing typed maps and KeyboardPanel consuming them (no magic strings).
Controller surface tightened: AppController no longer exposes timer getters, and requestNewGame/buildWordSelectionData are private to discourage UI from using GameConfig directly. Removed unused imports (e.g., GameMode) in GameSessionService.