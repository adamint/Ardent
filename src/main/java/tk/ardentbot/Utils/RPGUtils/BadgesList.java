package tk.ardentbot.Utils.RPGUtils;

import lombok.Getter;

public enum BadgesList {
    PREMIUM_TRIAL(1, "premiumtrial", "Premium One Day Trial", 7500, true);

    @Getter
    private int dayDuration;
    @Getter
    private String id;
    @Getter
    private double cost;
    @Getter
    private boolean oneTime;
    @Getter
    private String name;

    BadgesList(int dayDuration, String id, String name, double cost, boolean oneTime) {
        this.dayDuration = dayDuration;
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.oneTime = oneTime;
    }
}
