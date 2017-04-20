package tk.ardentbot.rethink.models;

import lombok.Getter;

public class TinderMatch {
    @Getter
    private String user_id;
    @Getter
    private String person_id;
    @Getter
    private boolean swipedRight;

    public TinderMatch(String user_id, String person_id, boolean swipedRight) {
        this.user_id = user_id;
        this.person_id = person_id;
        this.swipedRight = swipedRight;
    }
}
