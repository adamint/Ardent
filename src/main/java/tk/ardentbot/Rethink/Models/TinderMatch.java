package tk.ardentbot.Rethink.Models;

import lombok.Getter;

public class TinderMatch {
    @Getter
    private String user_id;
    @Getter
    private String person_id;
    @Getter
    private boolean swipedLeft;

    public TinderMatch(String user_id, String person_id, boolean swipedLeft) {
        this.user_id = user_id;
        this.person_id = person_id;
        this.swipedLeft = swipedLeft;
    }
}
