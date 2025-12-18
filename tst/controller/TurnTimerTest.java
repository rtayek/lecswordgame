package controller;

import controller.TimerController;
import controller.TurnTimer;
import model.Records.GamePlayer;
import model.Records.PlayerProfile;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Smoke test to ensure TurnTimer fans out events to multiple listeners.
 */
public final class TurnTimerTest {

    public static void main(String[] args) throws Exception {
        shouldNotifyAllListeners();
        System.out.println("TurnTimerTest passed");
    }

    private static void shouldNotifyAllListeners() throws Exception {
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

        // Wait for expiration; swing timer fires on EDT, so give a small window
        if (!expireLatch.await(2, TimeUnit.SECONDS)) {
            throw new AssertionError("Listeners did not receive expiry");
        }
        if (updateLatch.getCount() == 2) {
            throw new AssertionError("Listeners did not receive any update");
        }
    }
}
