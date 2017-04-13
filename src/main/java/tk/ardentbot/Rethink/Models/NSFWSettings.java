package tk.ardentbot.Rethink.Models;

import lombok.Getter;

import java.util.ArrayList;

public class NSFWSettings {
    @Getter
    private String guild_id;
    @Getter
    private ArrayList<String> nsfwChannels;
    @Getter
    private boolean global;
    @Getter
    private boolean needNsfwRole;

    public NSFWSettings(String guild_id) {
        this.guild_id = guild_id;
        this.nsfwChannels = new ArrayList<>();
        this.global = true;
        this.needNsfwRole = false;
    }
}
