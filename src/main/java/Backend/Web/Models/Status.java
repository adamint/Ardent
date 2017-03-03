package Backend.Web.Models;

/**
 * POJO Representation of /api/status
 */
public class Status {
    public long receivedMessages;
    public long receivedCommands;
    public long uptime;
    public int loaded_commands;
    public int guilds;
    public int users;

    public Status(long receivedMessages, long receivedCommands, long uptime, int loaded_commands, int guilds, int users) {
        this.receivedMessages = receivedMessages;
        this.receivedCommands = receivedCommands;
        this.uptime = uptime;
        this.loaded_commands = loaded_commands;
        this.guilds = guilds;
        this.users = users;
    }

}
