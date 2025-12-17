package controller;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.Timer;
import model.Records.GamePlayer;

public class TimerController {
	public interface Listener {
		void onTimeUpdated(GamePlayer player,int remainingSeconds);
		void onTimeExpired(GamePlayer player);
	}

	public TimerController() {
		ActionListener timerListener = e -> {
			if (activePlayer != null) {
				tick(activePlayer);
			}
		};
		swingTimer = new Timer(1000, timerListener);
	}

	public void setListener(Listener listener) {
		this.listener=listener;
	}

	public void setTimeForPlayer(GamePlayer player,int seconds) {
		remainingSeconds.put(player,seconds);
		notifyUpdate(player);
	}

	public int getRemainingFor(GamePlayer player) {
		return remainingSeconds.getOrDefault(player,0);
	}

	public void start(GamePlayer player) {
		this.activePlayer = player;
		swingTimer.start();
	}

	public void stop() {
		swingTimer.stop();
		this.activePlayer = null;
	}

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
		if(listener!=null) {
			listener.onTimeUpdated(player,getRemainingFor(player));
		}
	}

	private void notifyExpired(GamePlayer player) {
		if(listener!=null) {
			listener.onTimeExpired(player);
		}
	}

	private final Timer swingTimer;
	private final Map<GamePlayer,Integer> remainingSeconds=new HashMap<>();
	private Listener listener;
	private GamePlayer activePlayer;
}
