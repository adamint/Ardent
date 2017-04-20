package tk.ardentbot.rethink.models;

import lombok.Getter;

public class AutoroleModel {
    @Getter
    private String guild_id;
    @Getter
    private String name;
    @Getter
    private String role_id;

    public AutoroleModel(String guild_id, String name, String role_id) {
        this.guild_id = guild_id;
        this.name = name;
        this.role_id = role_id;
    }
}
