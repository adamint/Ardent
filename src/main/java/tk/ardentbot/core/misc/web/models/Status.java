package tk.ardentbot.core.misc.web.models;

/**
 * POJO Representation of /api/status
 */
public class Status {
    public long received_messages;
    public long received_commands;
    public long uptime;
    public long loaded_commands;
    public long guilds;
    public long users;
    public long role_count;
    public long text_channel_count;
    public long voice_channel_count;
    public long music_players;
    public Status(long receivedMessages, long receivedCommands, long uptime, long loaded_commands, long guilds, long
            users, long roleCount, long textChannelCount, long voiceChannelCount, long musicPlayers) {
        this.received_messages = receivedMessages;
        this.received_commands = receivedCommands;
        this.uptime = uptime;
        this.loaded_commands = loaded_commands;
        this.guilds = guilds;
        this.users = users;
        this.role_count = roleCount;
        this.text_channel_count = textChannelCount;
        this.voice_channel_count = voiceChannelCount;
        this.music_players = musicPlayers;
    }

}
