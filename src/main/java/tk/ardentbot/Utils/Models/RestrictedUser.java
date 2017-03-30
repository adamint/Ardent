package tk.ardentbot.Utils.Models;

import lombok.Getter;
import net.dv8tion.jda.core.entities.Guild;

@Getter
public class RestrictedUser {
    public String userId;
    public String restrictedById;
    public String guildId;

    public RestrictedUser(String userId, String restrictedById, Guild guild) {
        this.userId = userId;
        this.restrictedById = restrictedById;
        this.guildId = guild.getId();
    }
}
