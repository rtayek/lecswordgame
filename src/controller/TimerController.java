package controller;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.Timer;
import controller.events.PlayerSlot;

/**
 * Swing-backed implementation of TurnTimer.
 */
public class TimerController implements TurnTimer {

	public TimerController() {
		ActionListener timerListener = e -> {
			if (activeSlot != null) {
				tick(activeSlot);
			}
		};
		swingTimer = new Timer(1000, timerListener);
	}

	@Override
	public void addListener(Listener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(Listener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	@Override
	public void setTimeForPlayer(PlayerSlot slot,int seconds) {
		if (slot == null) return;
		remainingSeconds.put(slot,seconds);
		notifyUpdate(slot);
	}

	@Override
	public int getRemainingFor(PlayerSlot slot) {
		if (slot == null) return 0;
		return remainingSeconds.getOrDefault(slot,0);
	}

	@Override
	public void start(PlayerSlot slot) {
		this.activeSlot = slot;
		swingTimer.start();
	}

	@Override
	public void stop() {
		swingTimer.stop();
		this.activeSlot = null;
	}

	@Override
	public void reset() {
		stop();
		remainingSeconds.clear();
	}

	private void tick(PlayerSlot slot) {
		Objects.requireNonNull(slot,"slot");
		var current=getRemainingFor(slot);
		if(current<=0) { return; }
		var updated=current-1;
		remainingSeconds.put(slot,updated);
		if(updated<=0) {
			swingTimer.stop();
			notifyExpired(slot);
		} else {
			notifyUpdate(slot);
		}
	}

	private void notifyUpdate(PlayerSlot slot) {
		for (Listener l : listeners) {
			l.onTimeUpdated(slot,getRemainingFor(slot));
		}
	}

	private void notifyExpired(PlayerSlot slot) {
		for (Listener l : listeners) {
			l.onTimeExpired(slot);
		}
	}

	private final Timer swingTimer;
	private final Map<PlayerSlot,Integer> remainingSeconds=new HashMap<>();
	private final Set<Listener> listeners=new HashSet<>();
	private PlayerSlot activeSlot;
}
