package controller;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import model.Records.GamePlayer;
class TimerController {
	interface Listener {
		void onTimeUpdated(GamePlayer player,int remainingSeconds);
		void onTimeExpired(GamePlayer player);
	}
	void setListener(Listener listener) {
		this.listener=listener;
	}
	void setTimeForPlayer(GamePlayer player,int seconds) {
		remainingSeconds.put(player,seconds);
		notifyUpdate(player);
	}
	int getRemainingFor(GamePlayer player) {
		return remainingSeconds.getOrDefault(player,0);
	}
	void tick(GamePlayer player) {
		Objects.requireNonNull(player,"player");
		var current=getRemainingFor(player);
		if(current<=0) { return; }
		var updated=current-1;
		remainingSeconds.put(player,updated);
		if(updated<=0) {
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
	final Map<GamePlayer,Integer> remainingSeconds=new HashMap<>();
	Listener listener;
}
