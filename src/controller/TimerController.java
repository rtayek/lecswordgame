package controller;

import controller.events.PlayerSlot;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Timer implementation backed by a ScheduledExecutor (no Swing dependency).
 */
public class TimerController implements TurnTimer {

    private final ScheduledExecutorService executor;
    private final long tickMillis;
    private ScheduledFuture<?> ticker;
    private final Map<PlayerSlot, Integer> remainingSeconds = new HashMap<>();
    private final Set<Listener> listeners = new HashSet<>();
    private PlayerSlot activeSlot;

    /**
     * Constructs a timer with a 1-second tick interval.
     */
    public TimerController() {
        this(Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TurnTimer");
            t.setDaemon(true);
            return t;
        }), 1000);
    }

    /**
     * Package-private constructor for tests to inject executor and tick interval.
     */
    TimerController(ScheduledExecutorService executor, long tickMillis) {
        this.executor = Objects.requireNonNull(executor, "executor");
        this.tickMillis = tickMillis;
    }

    @Override
    public synchronized void addListener(Listener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public synchronized void removeListener(Listener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public synchronized void setTimeForPlayer(PlayerSlot slot, int seconds) {
        if (slot == null) return;
        remainingSeconds.put(slot, seconds);
        notifyUpdate(slot);
    }

    @Override
    public synchronized int getRemainingFor(PlayerSlot slot) {
        if (slot == null) return 0;
        return remainingSeconds.getOrDefault(slot, 0);
    }

    @Override
    public synchronized void start(PlayerSlot slot) {
        if (slot == null) return;
        this.activeSlot = slot;
        if (ticker != null) {
            ticker.cancel(false);
        }
        ticker = executor.scheduleAtFixedRate(this::tickScheduled, tickMillis, tickMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void stop() {
        if (ticker != null) {
            ticker.cancel(false);
            ticker = null;
        }
        this.activeSlot = null;
    }

    @Override
    public synchronized void reset() {
        stop();
        remainingSeconds.clear();
    }

    private void tickScheduled() {
        PlayerSlot slotSnapshot;
        synchronized (this) {
            slotSnapshot = activeSlot;
        }
        if (slotSnapshot != null) {
            tick(slotSnapshot);
        }
    }

    private synchronized void tick(PlayerSlot slot) {
        Objects.requireNonNull(slot, "slot");
        var current = getRemainingFor(slot);
        if (current <= 0) {
            return;
        }
        var updated = current - 1;
        remainingSeconds.put(slot, updated);
        if (updated <= 0) {
            stop();
            notifyExpired(slot);
        } else {
            notifyUpdate(slot);
        }
    }

    private void notifyUpdate(PlayerSlot slot) {
        // copy to avoid concurrent modification if listeners mutate set
        Listener[] snapshot;
        synchronized (this) {
            snapshot = listeners.toArray(Listener[]::new);
        }
        for (Listener l : snapshot) {
            l.onTimeUpdated(slot, getRemainingFor(slot));
        }
    }

    private void notifyExpired(PlayerSlot slot) {
        Listener[] snapshot;
        synchronized (this) {
            snapshot = listeners.toArray(Listener[]::new);
        }
        for (Listener l : snapshot) {
            l.onTimeExpired(slot);
        }
    }
}
