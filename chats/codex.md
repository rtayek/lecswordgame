Timer values for untimed games: GameUiModelMapper now only fills remaining time when the game is timed; untimed games leave remaining null.
Keyboard readability: KeyboardPanel now uses black text for unused keys (no background) and white only when a colored background is applied.
Outcome prompts avoid timeouts: Added FinishStateView fields to GameUiModel and updated GameOutcomePresenter to skip “Did you know the word?” prompts when a timeout/forfeit occurred.
Event handling clarity: Split listeners remain; removed the unused timerUpdated kind and handle timerExpired as a state update. Base panel now safely marshals UI updates onto the EDT.
Input gating: Multiplayer UI disables input while awaitingWinnerKnowledge to avoid rejected guesses.
Timer labels for untimed games: Solo/multiplayer timer labels are blanked when no timer is configured.
Cleanup: Removed redundant checks and unused params (e.g., startGame no longer takes unused player args).