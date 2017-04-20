package tk.ardentbot.rethink.models;

import lombok.Getter;

public class MusicSettingsModel {
    @Getter
    private String guild_id;
    @Getter
    private boolean remove_addition_messages;
    @Getter
    private String channel_id;

    public MusicSettingsModel(String guild_id, boolean remove_addition_messages, String channel_id) {
        this.guild_id = guild_id;
        this.remove_addition_messages = remove_addition_messages;
        this.channel_id = channel_id;
    }
}
