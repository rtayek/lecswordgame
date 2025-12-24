Added the requested invariant coverage and tightened behavior:

GameSessionService now ignores onTimeUpdated when the game is untimed (guard added).
New controller tests in GameSessionServiceTestCase:
Untimed games ignore timer updates (no events published).
Winner-knowledge flow is event-driven: awaiting knowledge -> apply winner knowledge -> game finishes via a gameFinished event, and the knowledge flag is recorded.
Also previously:

Timer test is hardened (cleanup in try/finally, longer awaits).
Keyboard disable test covers re-enable as well.
If you’d like, I can add a UI-level test for “untimed ticks don’t change labels,” but that will require some stubbing to construct the panels.