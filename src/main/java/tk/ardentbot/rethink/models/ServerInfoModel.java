package tk.ardentbot.rethink.models;

import lombok.Getter;

public class ServerInfoModel {
    @Getter
    private String guild_id;
    @Getter
    private String message;

    public ServerInfoModel(String guild_id, String message) {
        this.guild_id = guild_id;
        this.message = message;
    }
}
