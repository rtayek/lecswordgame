package controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import controller.events.PlayerSlot;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Deterministic-ish integration test for TimerController using short tick interval.
 */
public class TimerControllerTestCase {

    @Test
    @Timeout(5)
    void timerCountsDownAndNotifiesListener() throws Exception {
        var executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TurnTimerTest");
            t.setDaemon(true);
            return t;
        });
        TimerController timer = new TimerController(executor, 10); // 10 ms ticks for speed
        CountDownLatch updates = new CountDownLatch(2); // initial + one tick
        CountDownLatch expired = new CountDownLatch(1);

        timer.addListener(new TurnTimer.Listener() {
            @Override
            public void onTimeUpdated(PlayerSlot slot, int remainingSeconds) {
                if (slot == PlayerSlot.playerOne) {
                    updates.countDown();
                }
            }

            @Override
            public void onTimeExpired(PlayerSlot slot) {
                if (slot == PlayerSlot.playerOne) {
                    expired.countDown();
                }
            }
        });

        timer.setTimeForPlayer(PlayerSlot.playerOne, 2);
        timer.start(PlayerSlot.playerOne);

        assertTrue(updates.await(1, TimeUnit.SECONDS), "Should receive initial and tick updates");
        assertTrue(expired.await(1, TimeUnit.SECONDS), "Should receive expired callback");
        timer.stop();
        executor.shutdownNow();
    }
}
