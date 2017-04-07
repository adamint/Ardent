package tk.ardentbot.Rethink.Models;

import lombok.Getter;

public class OneTimeBadgeModel {
    @Getter
    private String user_id;
    @Getter
    private String badge_id;

    public OneTimeBadgeModel(String user_id, String badge_id) {
        this.user_id = user_id;
        this.badge_id = badge_id;
    }
}
