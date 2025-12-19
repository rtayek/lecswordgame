package controller;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import model.GamePlayer;
import model.PlayerProfile;

/**
 * JUnit 5 smoke test to ensure TurnTimer fans out events to multiple listeners.
 */
class TurnTimerTest {

    @Test
    void shouldNotifyAllListeners() throws Exception {
        TurnTimer timer = new TimerController();
        var player = new GamePlayer(new PlayerProfile("P1", ""), true);

        var updateLatch = new CountDownLatch(2); // two listeners, at least one update each
        var expireLatch = new CountDownLatch(2);

        TurnTimer.Listener listenerA = new TurnTimer.Listener() {
            @Override
            public void onTimeUpdated(GamePlayer p, int remainingSeconds) {
                updateLatch.countDown();
            }

            @Override
            public void onTimeExpired(GamePlayer p) {
                expireLatch.countDown();
            }
        };
        TurnTimer.Listener listenerB = new TurnTimer.Listener() {
            @Override
            public void onTimeUpdated(GamePlayer p, int remainingSeconds) {
                updateLatch.countDown();
            }

            @Override
            public void onTimeExpired(GamePlayer p) {
                expireLatch.countDown();
            }
        };

        timer.addListener(listenerA);
        timer.addListener(listenerB);
        timer.setTimeForPlayer(player, 1);
        timer.start(player);

        assertTrue(expireLatch.await(2, TimeUnit.SECONDS), "Listeners should receive expiry");
        assertNotEquals(2, updateLatch.getCount(), "Listeners should receive at least one update");
    }
}
