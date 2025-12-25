I unpacked the current tar and looked specifically for the architectural refactorings we discussed. Here’s what he actually did (with a quick assessment).

## What he changed

### 1) Added a centralized view→model mapper

New file:

* `src/controller/ViewToModelMapper.java` (package-private)

It centralizes conversions from DTOs to domain types:

* `WordChoiceView -> model.WordChoice` (including `WordSourceView -> WordSource`)
* `DifficultyView -> model.enums.Difficulty` (plus a nullable variant)
* `WordLengthView -> model.enums.WordLength`
* `TimerDurationView -> model.enums.TimerDuration`
* `GameLogEntryView -> model.GameLogEntry` (maps `wordLength` int back to `WordLength`)

This directly implements the “move mapping out of AppController” recommendation.

**Verdict:** Good change. It shrinks the surface area of “random conversion logic” and makes it easier to unit test and audit.

---

### 2) Made `AppController.addGameLogEntry` DTO-only

`AppController` now exposes:

```java
public void addGameLogEntry(GameLogEntryView entry)
```

and delegates to:

```java
profileService.addGameLogEntry(mapper.toModel(entry));
```

So the last obvious public “model leak” is gone.

**Verdict:** Good change. This aligns with your “DTO boundary” story and prevents accidental view/model coupling later.

---

### 3) Updated start/selection flows to use the mapper consistently

In `AppController`:

* `playerOneWordSelected` / `playerTwoWordSelected` now call `mapper.toModel(wordChoice)`
* `pickWord(length)` and `isValidWord(word, length)` now use `mapper.toModel(length)`
* `startSoloGame` / `startMultiplayerGame` use `mapper.toModel(difficulty/wordLength/timer)`

So conversions are no longer duplicated across the controller.

**Verdict:** Good. Consistency matters here.

---

## What I would still tweak (small, optional)

### A) Move `mapDifficulty` into the mapper (consistency polish)

`getGameLog()` still uses a private `mapDifficulty(model.enums.Difficulty)` in `AppController`. It’s fine, but slightly inconsistent now that you have a dedicated mapper.

**Optional:** add `DifficultyView toView(Difficulty)` to a small “ModelToViewMapper” or extend the existing mapper (renaming it if you do both directions).

### B) `ViewToModelMapper.toModel(GameLogEntryView)` could be simplified

It currently loops through `WordLength.values()` to find the matching length. That’s fine, but if you later add lengths, you’ll want a single utility like:

* `WordLength.fromLength(int)` (domain-side) or
* a tiny private helper in the mapper

Not urgent.

---

## Bottom line

He implemented the two “last mile” architectural improvements we identified:

1. **Centralize DTO→model mapping**
2. **Remove the remaining DTO boundary leak (`addGameLogEntry`)**

These are the right kind of changes: small, clarifying, low-risk, and they do not re-open the architecture loop.
