package controller;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.Timer;
import model.Records.GamePlayer;

/**
 * Swing-backed implementation of TurnTimer.
 */
public class TimerController implements TurnTimer {

	public TimerController() {
		ActionListener timerListener = e -> {
			if (activePlayer != null) {
				tick(activePlayer);
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
	public void setTimeForPlayer(GamePlayer player,int seconds) {
		remainingSeconds.put(player,seconds);
		notifyUpdate(player);
	}

	@Override
	public int getRemainingFor(GamePlayer player) {
		return remainingSeconds.getOrDefault(player,0);
	}

	@Override
	public void start(GamePlayer player) {
		this.activePlayer = player;
		swingTimer.start();
	}

	@Override
	public void stop() {
		swingTimer.stop();
		this.activePlayer = null;
	}

	@Override
	public void reset() {
		stop();
		remainingSeconds.clear();
	}

	private void tick(GamePlayer player) {
		Objects.requireNonNull(player,"player");
		var current=getRemainingFor(player);
		if(current<=0) { return; }
		var updated=current-1;
		remainingSeconds.put(player,updated);
		if(updated<=0) {
			swingTimer.stop();
			notifyExpired(player);
		} else {
			notifyUpdate(player);
		}
	}

	private void notifyUpdate(GamePlayer player) {
		for (Listener l : listeners) {
			l.onTimeUpdated(player,getRemainingFor(player));
		}
	}

	private void notifyExpired(GamePlayer player) {
		for (Listener l : listeners) {
			l.onTimeExpired(player);
		}
	}

	private final Timer swingTimer;
	private final Map<GamePlayer,Integer> remainingSeconds=new HashMap<>();
	private final Set<Listener> listeners=new HashSet<>();
	private GamePlayer activePlayer;
}
