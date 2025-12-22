Removed the last model leak from the timer path and made timer updates event-driven:

Introduced PlayerSlot DTO (PlayerSlot.java) and refactored TurnTimer/TimerController to use slots instead of model.GamePlayer (TurnTimer.java, TimerController.java).
GameSessionService now maps between domain players and slots, starts/stops the timer by slot, publishes UI snapshots on ticks, and handles expirations without sending model objects to views (GameSessionService.java).
Views no longer listen to the timer; they update timer labels from GameUiModel snapshots via a new updateTimersFromModel hook (BaseGamePanel.java, SoloGamePanel.java, MultiplayerGamePanel.java). This also removes the remaining model imports from the view layer.
Build/tests not run here (read-only environment); please rebuild to confirm everything compiles and timer labels update correctly via events.