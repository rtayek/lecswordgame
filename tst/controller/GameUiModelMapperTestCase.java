package controller;

import controller.events.GameUiModel;
import controller.events.PlayerSlot;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameUiModelMapper snapshot behavior.
 */
class GameUiModelMapperTestCase {

    @Test
    void untimedGamesDoNotExposeRemainingSeconds() {
        var p1 = new model.GamePlayer(new model.PlayerProfile("P1", ""), true);
        var p2 = new model.GamePlayer(new model.PlayerProfile("P2", ""), true);
        var config = new model.GameState.GameConfig(model.enums.GameMode.multiplayer,
                model.enums.Difficulty.normal,
                model.enums.WordLength.five,
                model.enums.TimerDuration.none,
                p1,
                p2);
        var state = new model.GameState(config);

        var mapper = new GameUiModelMapper(new StubTimer(Map.of()), new KeyboardViewBuilder());
        GameUiModel ui = mapper.toUiModel(state);

        assertNull(ui.playerOneRemaining(), "Untimed game should not expose remaining time for player one");
        assertNull(ui.playerTwoRemaining(), "Untimed game should not expose remaining time for player two");
    }

    @Test
    void timedGamesExposeRemainingSeconds() {
        var p1 = new model.GamePlayer(new model.PlayerProfile("P1", ""), true);
        var p2 = new model.GamePlayer(new model.PlayerProfile("P2", ""), true);
        var config = new model.GameState.GameConfig(model.enums.GameMode.multiplayer,
                model.enums.Difficulty.normal,
                model.enums.WordLength.five,
                model.enums.TimerDuration.oneMinute,
                p1,
                p2);
        var state = new model.GameState(config);

        var mapper = new GameUiModelMapper(new StubTimer(Map.of(
                PlayerSlot.playerOne, 42,
                PlayerSlot.playerTwo, 17
        )), new KeyboardViewBuilder());
        GameUiModel ui = mapper.toUiModel(state);

        assertEquals(42, ui.playerOneRemaining());
        assertEquals(17, ui.playerTwoRemaining());
        assertEquals(60, ui.timerDurationSeconds());
    }

    private static final class StubTimer implements TurnTimer {
        private final Map<PlayerSlot, Integer> remaining;
        StubTimer(Map<PlayerSlot, Integer> remaining) {
            this.remaining = remaining;
        }
        @Override public void addListener(Listener listener) { }
        @Override public void removeListener(Listener listener) { }
        @Override public void setTimeForPlayer(PlayerSlot slot, int seconds) { }
        @Override public int getRemainingFor(PlayerSlot slot) { return remaining.getOrDefault(slot, 0); }
        @Override public void start(PlayerSlot slot) { }
        @Override public void stop() { }
        @Override public void reset() { }
    }
}
