package controller;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import model.GamePlayer;
import model.PlayerProfile;

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

        if (!expireLatch.await(2, TimeUnit.SECONDS)) {
            throw new AssertionError("Listeners should receive expiry");
        }
        if (updateLatch.getCount() == 2) {
            throw new AssertionError("Listeners should receive at least one update");
        }
    }
}
