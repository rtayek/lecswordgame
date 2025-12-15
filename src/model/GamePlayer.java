package model;
import java.util.Objects;
public class GamePlayer {
	public GamePlayer(PlayerProfile profile,boolean human) {
		this.profile=profile;
		this.human=human;
	}
	public PlayerProfile getProfile() {
		return profile;
	}
	public boolean isHuman() {
		return human;
	}
	@Override public String toString() {
		var name=profile!=null?profile.getUsername():"Unknown";
		return (human?"Human":"AI")+": "+name;
	}
	@Override public boolean equals(Object o) {
		if(this==o) { return true; }
		if(!(o instanceof GamePlayer other)) { return false; }
		return human==other.human&&Objects.equals(profile,other.profile);
	}
	@Override public int hashCode() {
		return Objects.hash(profile,human);
	}
	final PlayerProfile profile;
	final boolean human;
}
