package tk.ardentbot.Core.Misc.WebServer.Models;

/**
 * POJO Representation of /api/status
 */
public class Status {
    public long receivedMessages;
    public long receivedCommands;
    public long uptime;
    public long loaded_commands;
    public long guilds;
    public long users;

    public Status(long receivedMessages, long receivedCommands, long uptime, long loaded_commands, long guilds, long
            users) {
        this.receivedMessages = receivedMessages;
        this.receivedCommands = receivedCommands;
        this.uptime = uptime;
        this.loaded_commands = loaded_commands;
        this.guilds = guilds;
        this.users = users;
    }

}
