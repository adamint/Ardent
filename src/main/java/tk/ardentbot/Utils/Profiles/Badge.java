package tk.ardentbot.Utils.Profiles;

import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Utils.Discord.UserUtils;

public class Badge {
    private String userId;
    private String id;
    private boolean guildWide;
    private long expirationEpochSeconds;

    public Badge(String userId, String id, boolean guildWide, long expirationEpochSeconds) {
        this.userId = userId;
        this.id = id;
        this.guildWide = guildWide;
        this.expirationEpochSeconds = expirationEpochSeconds;
    }

    public String getId() {
        return id;
    }

    public boolean isGuildWide() {
        return guildWide;
    }

    public long getExpirationEpochSeconds() {
        return expirationEpochSeconds;
    }

    public User getUser() {
        return UserUtils.getUserById(userId);
    }
}
