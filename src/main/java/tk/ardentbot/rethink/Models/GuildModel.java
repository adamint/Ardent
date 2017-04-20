package tk.ardentbot.rethink.models;

import lombok.Getter;

public class GuildModel {
    @Getter
    private String guild_id;
    @Getter
    private String language;
    @Getter
    private String prefix;

    public GuildModel(String guild_id, String language, String prefix) {
        this.guild_id = guild_id;
        this.language = language;
        this.prefix = prefix;
    }
}
