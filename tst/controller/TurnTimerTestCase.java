package controller;

import controller.events.PlayerSlot;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TurnTimerTestCase {

    @Test
    void shouldNotifyAllListeners() throws Exception {
        TurnTimer timer = new TimerController();
        var slot = PlayerSlot.playerOne;

        var updateLatch = new CountDownLatch(2); // two listeners, at least one update each
        var expireLatch = new CountDownLatch(2);

        TurnTimer.Listener listenerA = new TurnTimer.Listener() {
            @Override
            public void onTimeUpdated(PlayerSlot p, int remainingSeconds) {
                updateLatch.countDown();
            }

            @Override
            public void onTimeExpired(PlayerSlot p) {
                expireLatch.countDown();
            }
        };
        TurnTimer.Listener listenerB = new TurnTimer.Listener() {
            @Override
            public void onTimeUpdated(PlayerSlot p, int remainingSeconds) {
                updateLatch.countDown();
            }

            @Override
            public void onTimeExpired(PlayerSlot p) {
                expireLatch.countDown();
            }
        };

        timer.addListener(listenerA);
        timer.addListener(listenerB);
        timer.setTimeForPlayer(slot, 1);
        timer.start(slot);

        assertTrue(expireLatch.await(2, TimeUnit.SECONDS), "Listeners should receive expiry");
        assertTrue(updateLatch.getCount() < 2, "Listeners should receive at least one update");
    }
}
