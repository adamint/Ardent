package tk.ardentbot.akioTemporary.commands;

public enum ArdentCmdCategory {

    BotAdmin("Bot Administration"),
    BotInfo("Bot Information"),
    Fun("Fun"),
    GuildAdmin("Guild Administration"),
    GuildInfo("Guild Information"),
    Music("Music");

    private String name;

    ArdentCmdCategory(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
