package tk.ardentbot.utils.rpg;

import lombok.Getter;

public enum BadgesList {
    PREMIUM_TRIAL(1, "premiumtrial", "premium One Day Trial", "Buy a one day Tier 3 premium trial for $500", 500, false, true,
            "http://i" +
            ".imgur.com/maP6A0N.png");

    @Getter
    private int dayDuration;
    @Getter
    private String id;
    @Getter
    private double cost;
    @Getter
    private boolean guildWide;
    @Getter
    private boolean oneTime;
    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    private String imageUrl;

    BadgesList(int dayDuration, String id, String name, String description, double cost, boolean guildWide, boolean oneTime, String
            imageUrl) {
        this.dayDuration = dayDuration;
        this.id = id;
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.guildWide = guildWide;
        this.oneTime = oneTime;
        this.imageUrl = imageUrl;
    }

    public static BadgesList from(String id) {
        for (BadgesList b : BadgesList.values()) {
            if (b.id.equalsIgnoreCase(id)) return b;
        }
        return null;
    }
}
