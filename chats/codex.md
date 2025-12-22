Addressed the remaining concerns:

Confirmed timer is not exposed to the UI: Navigation and AppController no longer provide timer getters (field removed from AppController).
Timer ticks now publish only timerUpdated with a minimal TimerView; no full gameStateUpdated on each tick (GameSessionService).
GameEvent carries typed payloads and now has hasView()/hasTimer() helpers to make null-checking explicit.
KeyboardView is strongly typed (Map<Character, LetterFeedbackView>) and built via KeyboardViewBuilder; no string-based states remain.
Views handle timerUpdated via dedicated timer handlers.