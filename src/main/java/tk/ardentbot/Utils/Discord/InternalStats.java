package tk.ardentbot.Utils.Discord;

import tk.ardentbot.Core.CommandExecution.CommandFactory;
import tk.ardentbot.Main.Shard;

import static tk.ardentbot.Main.ShardManager.getShards;

public class InternalStats {
    private long messagesReceived;
    private long commandsReceived;
    private long loadedCommands;
    private long guilds;

    public InternalStats(long messagesReceived, long commandsReceived, long loadedCommands, long guilds) {
        this.messagesReceived = messagesReceived;
        this.commandsReceived = commandsReceived;
        this.loadedCommands = loadedCommands;
        this.guilds = guilds;
    }

    public static InternalStats collect() {
        long messagesReceived = 0;
        long commandsReceived = 0;
        long loadedCommands = 0;
        long guilds = 0;

        for (Shard shard : getShards()) {
            CommandFactory factory = shard.factory;
            messagesReceived += factory.getMessagesReceived();
            commandsReceived += factory.getCommandsReceived();
            if (loadedCommands == 0) loadedCommands = factory.getLoadedCommandsAmount();
            guilds += shard.jda.getGuilds().size();
        }
        return new InternalStats(messagesReceived, commandsReceived, loadedCommands, guilds);
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
}
