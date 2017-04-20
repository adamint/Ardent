package tk.ardentbot.rethink.models;

import lombok.Getter;
public class Patron {
    @Getter
    private String user_id;
    @Getter
    private String tier;

    public Patron(String user_id, String tier) {
        this.user_id = user_id;
        this.tier = tier;
    }
}
