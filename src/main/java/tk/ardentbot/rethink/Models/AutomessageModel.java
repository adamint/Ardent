package tk.ardentbot.rethink.models;

import lombok.Getter;

public class AutomessageModel {
    @Getter
    private String guild_id;
    @Getter
    private String channel_id;
    @Getter
    private String welcome;
    @Getter
    private String goodbye;

    public AutomessageModel(String guild_id, String channel_id, String welcome, String goodbye) {
        this.guild_id = guild_id;
        this.channel_id = channel_id;
        this.welcome = welcome;
        this.goodbye = goodbye;
    }
}
