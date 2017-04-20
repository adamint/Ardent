package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.main.Shard;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.List;

public class Unmute extends Command {
    public Unmute(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        Shard shard = GuildUtils.getShard(guild);
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("unmute", language, "help").getTranslation().replace("{0}",
                    GuildUtils.getPrefix(guild) + args[0]), channel, user);
        }
        else {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 0) {
                sendRetrievedTranslation(channel, "other", language, "mentionuser", user);
            }
            else {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    User mentioned = mentionedUsers.get(0);
                    Member m = guild.getMember(mentioned);

                    if (shard.botMuteData.isMuted(m)) {
                        shard.botMuteData.unmute(m);
                        sendRetrievedTranslation(channel, "unmute", language, "unmuteduser", user);
                    }
                    else {
                        sendRetrievedTranslation(channel, "unmute", language, "notmuted", user);
                    }

                }
                else sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
