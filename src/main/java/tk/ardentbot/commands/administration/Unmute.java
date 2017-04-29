package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.List;

public class Unmute extends Command {
    public Unmute(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        Shard shard = GuildUtils.getShard(guild);
        if (args.length == 1) {
            sendTranslatedMessage("Type {0}unmute @User to unmute them".replace("{0}",
                    GuildUtils.getPrefix(guild) + args[0]), channel, user);
        }
        else {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 0) {
                sendTranslatedMessage("You need to mention a user to unmute!", channel, user);
            }
            else {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    User mentioned = mentionedUsers.get(0);
                    Member m = guild.getMember(mentioned);

                    if (shard.botMuteData.isMuted(m)) {
                        shard.botMuteData.unmute(m);
                        sendTranslatedMessage("Unmuted that user.", channel, user);
                    }
                    else {
                        sendTranslatedMessage("That person isn't muted!", channel, user);
                    }

                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
