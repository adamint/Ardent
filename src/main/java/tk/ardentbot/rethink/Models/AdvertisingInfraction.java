package tk.ardentbot.rethink.models;

import lombok.Getter;

public class AdvertisingInfraction {
    @Getter
    private String user_id;
    @Getter
    private String guild_id;

    public AdvertisingInfraction(String user_id, String guild_id) {
        this.user_id = user_id;
        this.guild_id = guild_id;
    }
}
