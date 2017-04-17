package tk.ardentbot.rethink.models;

import lombok.Getter;

public class AntiAdvertisingSettings {
    @Getter
    private String guild_id;
    @Getter
    private boolean allow_discord_server_links;
    @Getter
    private boolean allow_all_links;
    @Getter
    private boolean ban_after_two_infractions;

    public AntiAdvertisingSettings(String guild_id, boolean allow_discord_server_links, boolean allow_all_links, boolean
            ban_after_two_infractions) {
        this.guild_id = guild_id;
        this.allow_discord_server_links = allow_discord_server_links;
        this.allow_all_links = allow_all_links;
        this.ban_after_two_infractions = ban_after_two_infractions;
    }
}
