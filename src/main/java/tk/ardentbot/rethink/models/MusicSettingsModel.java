package tk.ardentbot.rethink.models;

import lombok.Getter;
import lombok.Setter;

public class MusicSettingsModel {
    @Getter
    public String guild_id;
    @Getter
    public boolean remove_addition_messages;
    @Getter
    public String channel_id;
    @Getter
    @Setter
    public boolean announce_music;

    public MusicSettingsModel(String guild_id, boolean remove_addition_messages, String channel_id) {
        this.guild_id = guild_id;
        this.remove_addition_messages = remove_addition_messages;
        this.channel_id = channel_id;
        this.announce_music = false;
    }
}
