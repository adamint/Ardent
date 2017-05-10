package tk.ardentbot.rethink.models;

import lombok.Getter;

public class Staff {
    @Getter
    private String id;
    @Getter
    private String role;

    public Staff(String id, String roleToAdd) {
        this.id = id;
        this.role = roleToAdd;
    }
}
