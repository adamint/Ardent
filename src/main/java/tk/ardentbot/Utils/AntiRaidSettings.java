package tk.ardentbot.Utils;

/**
 * Upcoming
 */
public class AntiRaidSettings {
    private String guildId;
    private boolean enabled;
    private int minutesBeforeSpeaking;
    private int level;

    public AntiRaidSettings(String guildId, boolean enabled, int minutesBeforeSpeaking, int level) {
        this.guildId = guildId;
        this.enabled = enabled;
        this.minutesBeforeSpeaking = minutesBeforeSpeaking;
        this.level = level;
    }

    public String getGuildId() {
        return guildId;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public int getMinutesBeforeSpeaking() {
        return minutesBeforeSpeaking;
    }
    public int getLevel() {
        return level;
    }
}
