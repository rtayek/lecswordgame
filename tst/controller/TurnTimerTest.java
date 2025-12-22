package controller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import controller.events.PlayerSlot;

/**
 * Simple smoke test without JUnit dependency.
 */
public final class TurnTimerTest {

    public static void main(String[] args) throws Exception {
        shouldNotifyAllListeners();
        System.out.println("TurnTimerTest passed");
    }

    private static void shouldNotifyAllListeners() throws Exception {
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

        if (!expireLatch.await(2, TimeUnit.SECONDS)) {
            throw new AssertionError("Listeners should receive expiry");
        }
        if (updateLatch.getCount() == 2) {
            throw new AssertionError("Listeners should receive at least one update");
        }
    }
}
