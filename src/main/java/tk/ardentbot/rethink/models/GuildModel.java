package tk.ardentbot.rethink.models;

import lombok.Getter;

import java.util.ArrayList;

public class GuildModel {
    @Getter
    public ArrayList<RolePermission> role_permissions;
    @Getter
    public String guild_id;
    @Getter
    public String language;
    @Getter
    public String prefix;
    public GuildModel(String guild_id, String language, String prefix) {
        this.guild_id = guild_id;
        this.language = language;
        this.prefix = prefix;
        role_permissions = new ArrayList<>();
    }
}
