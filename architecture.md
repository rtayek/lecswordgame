Architecture Frozen Note
Status

As of this commit, the core architecture for the Word Guessing Game is frozen. Future changes should be feature-driven. Refactors that alter event flow, DTO boundaries, or threading policy require explicit justification and a dedicated review.

Core Model

The application follows a strict pattern:

Intent in: UI sends user intent to the controller (e.g., submitGuess, start game, report winner knowledge).

Events + snapshots out: UI renders only from controller-published events carrying DTO snapshots (GameUiModel) and timer updates (TimerView).

No UI inference: Views do not compute game outcomes from return values.

Layering Rules
View layer (src/view/**)

Views render from DTOs only:

GameUiModel, GuessView, KeyboardView, and view enums.

Views must not import domain/model classes:

No import model.* in src/view/**.

Views do not pull state from services or timers directly.

Views are responsible for EDT marshaling (see Threading Policy).

Controller/service layer (src/controller/**)

Controllers accept intents and orchestrate state transitions.

Controllers publish events containing DTO snapshots; they do not expose internal stateful objects.

Controllers/services must not depend on Swing.

Domain/model layer (src/model/**)

Owns the game rules and state transitions.

Does not depend on UI or controller code.

Threading Policy (EDT)

Policy: Views marshal to the Swing EDT.

Controllers/services may invoke listeners from any thread.

All view listener entry points must marshal to EDT before touching Swing components.

EDT marshaling is implemented in the view layer via a helper (e.g., UiThread.run(...)).

Controllers/services/timers must not call SwingUtilities.invokeLater.

Rationale: keeps controller/service layers UI-agnostic and preserves flexibility for future UIs.

Event Contract

GameEventListener is split and explicit:

onGameStateEvent(GameEventKind kind, GameUiModel model)

onTimerEvent(TimerView timerView)

Event kinds are minimal and semantic; redundant kinds were removed (e.g., no timerExpired).

Timers

Timer identity is UI-safe and does not leak domain types:

uses PlayerSlot, not model.GamePlayer.

Timer ticks are for countdown display only.

Timeout messaging and outcomes are authoritative via finish/state events, not via timer tick logic.

Untimed games must not display 00:00 and must ignore timer tick events.

Input Enable/Disable Invariants

Disabling KeyboardPanel must disable all child buttons (setEnabled propagates).

Finished states disable all relevant inputs (guess field, keyboard, submit, backspace).

Views must not mutate input fields via helper callbacks when inputs are disabled.

Test Expectations

Unit tests should protect:

DTO snapshot contracts (GameUiModelMapperTestCase and related)

state transition invariants (controller/service tests)

key regressions (e.g., keyboard disable propagation)
Avoid:

brittle Swing layout tests

reflection-based tests of private state (prefer package-private test hooks if needed)
Timer tests must be written to avoid flakiness (use deterministic scheduling where practical).

What is explicitly not allowed without review

Reintroducing domain types into the view layer

Returning game outcomes from controller calls for UI logic
Provide at least one:

observed bug that cannot be fixed within the frozen architecture, or

new requirement (e.g., networking, persistence, alternate UI) that forces the change

What to do next

Focus on feature work:

hardest/best words list + data collection

game logs screen and persistence

friends/profile flows

UX polish (sounds, animations, prompts)