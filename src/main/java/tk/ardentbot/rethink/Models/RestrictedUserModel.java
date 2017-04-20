package tk.ardentbot.rethink.models;

import lombok.Getter;

public class RestrictedUserModel {
    @Getter
    private String guild_id;
    @Getter
    private String user_id;
    @Getter
    private String restricter_id;

    public RestrictedUserModel(String guild_id, String user_id, String restricter_id) {
        this.guild_id = guild_id;
        this.user_id = user_id;
        this.restricter_id = restricter_id;
    }
}
