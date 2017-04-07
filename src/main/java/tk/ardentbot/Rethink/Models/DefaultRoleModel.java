package tk.ardentbot.Rethink.Models;

import lombok.Getter;

public class DefaultRoleModel {
    @Getter
    private String guild_id;
    @Getter
    private String role_id;

    public DefaultRoleModel(String guild_id, String role_id) {
        this.guild_id = guild_id;
        this.role_id = role_id;
    }
}
