package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.utils.discord.GuildUtils;

import java.util.List;

public class Kick extends Command {
    public Kick(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage("Kick users by typing **{0}kick** and then mentioning one or more users. The bot must have permission to kick users".replace("{0}", GuildUtils
                    .getPrefix(guild) + args[0]), channel, user);
        } else {
            Member userMember = guild.getMember(user);
            if (userMember.hasPermission(Permission.KICK_MEMBERS)) {
                List<User> mentionedUsers = message.getMentionedUsers();
                if (mentionedUsers.size() == 0) {
                    sendTranslatedMessage("You need to mention at least one user!", channel, user);
                } else {
                    for (User mentioned : mentionedUsers) {
                        if (!guild.getMember(mentioned).hasPermission(userMember.getPermissions((Channel) channel))) {
                            guild.getController().kick(guild.getMember(mentioned)).queue(aVoid -> {
                                try {
                                    sendTranslatedMessage("Succesfully kicked **{0}**".replace("{0}", mentioned.getName()), channel, user);
                                } catch (Exception e) {
                                    new BotException(e);
                                }
                            }, throwable -> {
                                try {
                                    sendTranslatedMessage("I wasn't able to kick **{0}**".replace("{0}", mentioned.getName()), channel, user);
                                } catch (Exception e) {
                                    new BotException(e);
                                }
                            });
                        } else sendTranslatedMessage("I could not kick him.", channel, user);
                    }
                }
            } else sendTranslatedMessage("You need permissions to kick!", channel, user);
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
