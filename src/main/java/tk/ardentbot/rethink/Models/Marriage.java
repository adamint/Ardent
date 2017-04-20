package tk.ardentbot.rethink.models;

import lombok.Getter;

public class Marriage {
    @Getter
    private String user_one;
    @Getter
    private String user_two;

    public Marriage(String user_one, String user_two) {
        this.user_one = user_one;
        this.user_two = user_two;
    }
}
