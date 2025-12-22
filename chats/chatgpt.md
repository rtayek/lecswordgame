What’s solid now
Timer boundary is finally correct

GameSessionService publishes timerUpdated with a TimerView(slot, remainingSeconds) and does not republish full GameUiModel on every tick.

UI does not receive a live timer controller through Navigation anymore (timer interaction is effectively service-owned).

This is the right architecture for “snapshot-driven UI + explicit timer events.”

Keyboard is typed end-to-end

KeyboardView(Map<Character, LetterFeedbackView>)

KeyboardViewBuilder merges feedback with a strength ordering and returns Map.copyOf(...)

This removes the “magic string” regression entirely.

Mapping surface exists and is reasonably contained

GameUiModelMapper is package-private and does domain→view conversion in one place.

GameSessionService.publish(...) always emits immutable DTOs.

Good.

Top recommendations
1) Fix GameEvent “nullable union” design

Right now you emit:

state events: new GameEvent(kind, uiModel, null)

timer events: new GameEvent(timerUpdated, null, timerView)

That forces every listener to remember which field is non-null. It’s easy to get wrong and it spreads null checks into UI code.

Preferred options (pick one):

Option A: sealed event hierarchy (cleanest)

sealed interface GameEvent permits GameStateEvent, TimerEvent {
    GameEventKind kind();
}

record GameStateEvent(GameEventKind kind, GameUiModel view) implements GameEvent { }
record TimerEvent(GameEventKind kind, TimerView timer) implements GameEvent { }


Option B: split listener methods (simplest for Swing)

interface GameEventListener {
    void onGameStateEvent(GameEventKind kind, GameUiModel view);
    void onTimerEvent(TimerView timer);
}


Either way, you eliminate “nullable payload roulette.”

2) Make AppController DTO-only on its outward-facing surface

You still have:

public void requestNewGame(GameConfig config)
public WordSelectionViewData buildWordSelectionData(GameConfig config, ...)


That exposes model.GameState.GameConfig to UI-adjacent code and keeps the door open for domain leakage.

You already mostly fixed this with:

startSoloGame(... DifficultyView, WordLengthView, TimerDurationView)

startMultiplayerGame(...)

Recommendation:

make requestNewGame(GameConfig) package-private (or delete it)

introduce StartGameRequest (in controller.events or controller.api) that uses only view enums / primitives

keep GameConfig construction strictly inside controller/service layer

This makes the “UI contract” enforceable by the compiler.

3) Unify timer rendering: prefer model snapshot for “initial”, timer event for “ticks”

In BaseGamePanel you currently:

call updateTimersFromModel(model) on gameStarted / stateUpdated

call updateTimersFromTimer(timerView) on timerUpdated

That’s correct, but make sure every timer label can be derived from:

snapshot: “initial remaining / duration”

timer event: “slot + remaining”

If any panel is still peeking elsewhere, delete that path.

4) Reduce UI’s need to “interpret” difficulty rules

KeyboardPanel.getButtonColor(...) hard-codes display semantics that depend on difficulty (“present is green in hard” etc.). That’s presentation logic, so it’s not wrong, but it’s also a policy decision.

If you want rendering to be totally dumb:

push a KeyboardColorView (or a derived per-key “display state”) into the DTO

or add a LetterFeedbackDisplayView enum that already accounts for difficulty

Otherwise, keep it as-is, but understand you’ve embedded game UI policy in Swing code.

5) Tighten some small hygiene items

GameSessionService imports model.enums.GameMode but doesn’t use it.

GameSessionService imports controller.KeyboardViewBuilder explicitly though it’s same package (harmless, but noisy).

BaseGamePanel.mapDifficulty(...) just null-coalesces; you can inline it and remove the method.

None of these are existential; they just reduce visual noise.

Priority order I’d execute

Fix GameEvent nullability (sealed events or split callbacks)

DTO-only AppController public API (remove/privatize GameConfig exposure)

Optional: push difficulty-dependent keyboard display policy into DTO (if you want a “dumb terminal” UI)

Hygiene pass (unused imports, tiny redundancies)