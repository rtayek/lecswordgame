package model;

public record GamePlayer(PlayerProfile profile, boolean human) {

	@Override public String toString() {
		var name=profile!=null?profile.username():"Unknown";
		return (human?"Human":"AI")+": "+name;
	}
}
