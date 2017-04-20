package tk.ardentbot.rethink.models;

import lombok.Getter;

public class Tag {
    @Getter
    private String guild_id;
    @Getter
    private String name;
    @Getter
    private String response;
    @Getter
    private String creator_id;

    public Tag(String guild_id, String name, String response, String creator_id) {
        this.guild_id = guild_id;
        this.name = name;
        this.response = response;
        this.creator_id = creator_id;
    }
}
