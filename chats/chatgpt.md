I took one last pass over the **current tar** with the mindset: “Is there anything *architectural* left that will create churn later?” You are in good shape. There are only a few **high-level refactorings** I would still consider, and all of them are optional and low risk.

## 1) Tighten the public controller API surface (small, worthwhile)

Right now `AppController` is clean for gameplay intent, but it still has one “domain leak” on the public surface:

* `public void addGameLogEntry(model.GameLogEntry entry)`

Even though the **view does not call it today**, it’s a trap door.

**Recommendation**
Change it to DTO-only:

* `addGameLogEntry(GameLogEntryView entry)` (or hide it entirely and let the controller write logs internally on `gameFinished`)

This is the last easy way for `model.*` to leak outward.

## 2) Move the view→model enum mapping out of `AppController` (clarity + testability)

`AppController` contains mapping methods `toModel(DifficultyView/WordLengthView/TimerDurationView/WordChoiceView)`.

**Recommendation**
Create a single mapper class in controller, e.g. `ViewToModelMapper`, and inject/use it. This:

* shrinks `AppController`
* centralizes conversion rules
* makes it easy to unit test mapping once

No behavior change; purely readability and maintainability.

## 3) Split `AppController` into three focused facades (only if it keeps growing)

`AppController` is already the largest class. It currently mixes:

* navigation orchestration (`NavigationCoordinator`, flow)
* gameplay (`GameSessionService`)
* profile/persistence (`ProfileService`)

**Recommendation (optional)**
If you add friends/logging/hardest-words features soon, consider splitting into:

* `GameFacade` (start game, submit guess, report knowledge, listeners)
* `ProfileFacade` (current profile, logs, hardest words)
* `NavigationFacade` (set navigation, word selection flow)

Then `AppController` becomes a thin façade that wires these together. This avoids “god object” drift.

If you’re not adding those features soon, skip this.

## 4) Decide what to do with `src/cli/CommandLineGame.java`

You said CLI is low priority. Leaving it around is fine, but it increases cognitive load and can rot.

**Recommendation**
Either:

* move CLI into `src/experimental/` (or similar), or
* delete it until you actually prioritize CLI again

This is about project hygiene, not architecture correctness.

## 5) Timer tests: keep them deterministic (only if flakes appear)

Your architecture is good; the only place that tends to get flaky over time is timer-related tests.

**Recommendation**
If you see intermittent reds, make `TimerController` accept a scheduler/clock abstraction so tests can advance time deterministically. If tests are stable today, do nothing.

---

# Bottom line

No major architectural work is warranted now. If you do *anything*, do **(1)** and **(2)**: they are small, reduce future regressions, and keep the “DTO boundary” story consistent.

Everything else is “only if the next feature wave makes it necessary.”
