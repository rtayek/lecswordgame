package model;

import java.util.Objects;

public class PlayerProfile {

    public PlayerProfile(String username, String avatarPath) {
        this.username = username;
        this.avatarPath = avatarPath;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(username, avatarPath);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PlayerProfile other)) {
            return false;
        }
        return Objects.equals(username, other.username) && Objects.equals(avatarPath, other.avatarPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, avatarPath);
    }

    final String username;
    final String avatarPath;
}
