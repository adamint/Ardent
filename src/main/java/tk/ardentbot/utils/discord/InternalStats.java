package tk.ardentbot.utils.discord;

import tk.ardentbot.core.executor.CommandFactory;
import tk.ardentbot.main.Shard;

import static tk.ardentbot.main.ShardManager.getShards;

public class InternalStats {
    private long messagesReceived;
    private long commandsReceived;
    private long loadedCommands;
    private long guilds;
    private long users;

    public InternalStats(long messagesReceived, long commandsReceived, long loadedCommands, long guilds, long users) {
        this.messagesReceived = messagesReceived;
        this.commandsReceived = commandsReceived;
        this.loadedCommands = loadedCommands;
        this.guilds = guilds;
        this.users = users;
    }

    public static InternalStats collect() {
        long messagesReceived = 0;
        long commandsReceived = 0;
        long loadedCommands = 0;
        long guilds = 0;
        long users = 0;
        for (Shard shard : getShards()) {
            CommandFactory factory = shard.factory;
            messagesReceived += factory.getMessagesReceived();
            commandsReceived += factory.getCommandsReceived();
            if (loadedCommands == 0) loadedCommands = factory.getLoadedCommandsAmount();
            guilds += shard.jda.getGuilds().size();
            users += shard.jda.getUsers().size();
        }
        return new InternalStats(messagesReceived, commandsReceived, loadedCommands, guilds, users);
    }

    public long getMessagesReceived() {
        return messagesReceived;
    }

    public long getCommandsReceived() {
        return commandsReceived;
    }

    public long getLoadedCommands() {
        return loadedCommands;
    }

    public long getGuilds() {
        return guilds;
    }

    public long getUsers() {
        return users;
    }
}
