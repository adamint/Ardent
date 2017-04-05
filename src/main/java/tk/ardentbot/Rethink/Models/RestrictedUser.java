package tk.ardentbot.Rethink.Models;

import lombok.Getter;

public class RestrictedUser {
    @Getter
    private String guild_id;
    @Getter
    private String user_id;
    @Getter
    private String restricter_id;
}
