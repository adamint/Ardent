package tk.ardentbot.akioTemporary.commands;

public enum ArdentCmdCategory {

    BotAdmin("Bot Administration"),
    BotInfo("Bot Information"),
    Fun("fun"),
    GuildAdmin("Guild Administration"),
    GuildInfo("Guild Information"),
    Music("music");

    private String name;

    ArdentCmdCategory(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
