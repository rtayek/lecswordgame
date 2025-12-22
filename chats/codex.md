Updated the tests to match the PlayerSlot-based timer API:

GameSessionServiceTest uses PlayerSlot for onTimeExpired, and its NoopTimer implements the new TurnTimer signatures (setTimeForPlayer/start/getRemainingFor with PlayerSlot).
TurnTimerTest now uses PlayerSlot instead of GamePlayer, updates listeners to the new method signatures, and sets/starts the timer with a slot.
Rebuild should clear the 17 errors you listed.