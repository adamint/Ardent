package tk.ardentbot.commands.music;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;

import java.util.List;

import static tk.ardentbot.commands.music.Music.getOutputChannel;
import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class MusicOutput extends Command {
    public MusicOutput(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage("Mention a channel to set the music output to it. Type `/music setoutput none` to remove any " +
                    "set " +
                    "output channel.", channel, user);
        }
        else {
            if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                List<TextChannel> mentionedChannels = message.getMentionedChannels();
                if (mentionedChannels.size() > 0) {
                    getOutputChannel(guild);
                    r.db("data").table("music_settings").filter(row -> row.g("guild_id").eq(guild.getId()))
                            .update(r.hashMap("channel_id", mentionedChannels.get(0).getId())).run(connection);
                    sendTranslatedMessage("Successfully set the output channel", channel, user);
                }
                else {
                    if (getOutputChannel(guild) != null) {
                        r.db("data").table("music_settings").filter(row -> row.g("guild_id").eq(guild.getId()))
                                .update(r.hashMap("channel_id", "none")).run(connection);
                        sendTranslatedMessage("Successfully removed the output channel", channel, user);
                    }
                    else {
                        sendTranslatedMessage("No output channel has been set for this server", channel, user);
                    }
                }
            }
            else sendTranslatedMessage("You need the Manage Server permission to do this", channel, user);
        }
    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
