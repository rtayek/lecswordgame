Centralized model→view difficulty mapping: ViewToModelMapper now has toView(Difficulty); AppController.getGameLog() uses it (removed the local mapDifficulty).
Simplified word-length mapping with a helper in ViewToModelMapper (no manual loop in toModel(GameLogEntryView)).
ViewToModelMapper gains a small fromLength helper and keeps other logic unchanged.