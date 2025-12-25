I unpacked the current tar and reviewed the *actual* changes (not the chat summaries). Here’s what he did, and whether it was a net improvement.

## What he actually changed

### 1) Added `ViewToModelMapper` (good)

New file: `src/controller/ViewToModelMapper.java` (package-private)

It centralizes DTO → domain conversions for:

* `WordChoiceView` → `model.WordChoice` (including `WordSourceView`)
* `DifficultyView` → `model.enums.Difficulty` (+ a nullable variant)
* `WordLengthView` → `model.enums.WordLength`
* `TimerDurationView` → `model.enums.TimerDuration`
* `GameLogEntryView` → `model.GameLogEntry`
* plus a `toView(Difficulty)` mapping back to `DifficultyView`

This is exactly the “stop sprinkling mapping logic across AppController” refactoring.

### 2) Changed `AppController.addGameLogEntry` to DTO-only (good)

`AppController` now exposes:

```java
public void addGameLogEntry(GameLogEntryView entry)
```

and maps it internally to `model.GameLogEntry` via the mapper before calling `ProfileService`.

This removes the obvious “model leak” from the controller surface.

### 3) Updated `AppController` to use the mapper for start/selection flows (good)

He replaced a bunch of ad-hoc conversions with `mapper.toModel(...)` calls:

* word selection (`playerOneWordSelected`, `playerTwoWordSelected`)
* dictionary ops (`pickWord`, `isValidWord`)
* game start config (`startSoloGame`, `startMultiplayerGame`)
* game log view (`getGameLog`) uses `mapper.toView(e.difficulty())`

### 4) Added `ViewToModelMapperTestCase` (good)

New test: `tst/controller/ViewToModelMapperTestCase.java`

It covers:

* defaults (null difficulty/length/timer)
* word choice source mapping
* log entry mapping including null/unknown word lengths

This is the right level: mapping is now a unit-tested contract.

## Quality notes / recommendations

### A) `fromLength(int)` currently returns `null` for unknown lengths

In `ViewToModelMapper`, `fromLength(7)` returns `null`. The tests assert this behavior.

That’s fine, but decide what you want long-term:

* If you want “unknown” to be representable: `null` is OK.
* If you want “always a valid enum”: pick a default (e.g., `five`) and test that instead.

Given you’re already using `0` in `getGameLog()` when `wordLength == null`, your current approach is consistent.

### B) Mapper is currently “mostly” one-way

You have `toView(Difficulty)` but not the other model→view mappings. That’s not wrong—just a smell that mapping may creep back into `AppController` as you add features.

If you notice more model→view mapping appearing, either:

* rename the mapper to something neutral (e.g., `ViewModelMapper`) and add both directions, or
* split cleanly into `ViewToModelMapper` and `ModelToViewMapper`.

Not urgent.

### C) Minor hygiene: `AppController` has a couple of unused imports

`AppController` imports `model.enums.Difficulty` but doesn’t use it (in the version in this tar). Eclipse will flag it.

## Bottom line

This was a **good** “high-level refactoring” pass:

* reduced API leakage
* centralized conversion logic
* added tests for the conversions
* did not disturb event flow / EDT policy / DTO-only rendering

If you keep anything from this round, keep the mapper + the DTO-only log entry API + the mapper tests.
