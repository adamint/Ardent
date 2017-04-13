package tk.ardentbot.Core.CommandExecution;

/**
 * The main command categories
 */
public enum Category {
    FUN,
    BOTINFO,
    BOTADMINISTRATION,
    GUILDADMINISTRATION,
    GUILDINFO,
    RPG,
    NSFW;

    public static final String FUN_CATEGORY = "fun";
    public static final String BOT_ADMINISTRATION = "botadministration";
    public static final String BOT_INFO = "botinfo";
    public static final String GUILD_ADMINISTRATION = "guildadministration";
    public static final String GUILD_INFO = "guildinfo";
    public static final String RPG_NAME = "rpg";
    public static final String NSFW_NAME = "NSFW";

    public static String getName(Category category) {
        if (category == FUN) return FUN_CATEGORY;
        else if (category == BOTADMINISTRATION) return BOT_ADMINISTRATION;
        else if (category == BOTINFO) return BOT_INFO;
        else if (category == GUILDADMINISTRATION) return GUILD_ADMINISTRATION;
        else if (category == GUILDINFO) return GUILD_INFO;
        else if (category == RPG) return RPG_NAME;
        else if (category == NSFW) return NSFW_NAME;
        else return null;
    }
}