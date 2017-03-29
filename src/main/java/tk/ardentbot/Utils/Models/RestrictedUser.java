package tk.ardentbot.Utils.Models;

import lombok.Data;

@Data(staticConstructor = "from")
public class RestrictedUser {
    private String userId;
    private String restrictedById;
    private String guildId;
}
