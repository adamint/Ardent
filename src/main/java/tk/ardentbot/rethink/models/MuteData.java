package tk.ardentbot.rethink.models;

import lombok.Getter;

public class MuteData {
    @Getter
    private String guild_id;
    @Getter
    private String user_id;
    @Getter
    private long unmute_epoch_second;
    @Getter
    private String muted_by_id;
}
