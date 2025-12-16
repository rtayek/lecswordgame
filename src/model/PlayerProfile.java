package model;

public record PlayerProfile(String username, String avatarPath) {

    @Override
    public String toString() {
        return "%s (%s)".formatted(username, avatarPath);
    }
}
