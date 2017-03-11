package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.List;

public class Kick extends Command {
    public Kick(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("kick", language, "help").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else {
            Member userMember = guild.getMember(user);
            if (userMember.hasPermission(Permission.KICK_MEMBERS)) {
                List<User> mentionedUsers = message.getMentionedUsers();
                if (mentionedUsers.size() == 0) {
                    sendRetrievedTranslation(channel, "other", language, "mentionuserorusers");
                }
                else {
                    for (User mentioned : mentionedUsers) {
                        if (!guild.getMember(mentioned).hasPermission(userMember.getPermissions((Channel) channel))) {
                            guild.getController().kick(guild.getMember(mentioned)).queue(aVoid -> {
                                try {
                                    sendTranslatedMessage(getTranslation("kick", language, "successfullykicked").getTranslation().replace("{0}", mentioned.getName()), channel);
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                            }, throwable -> {
                                try {
                                    sendTranslatedMessage(getTranslation("kick", language, "failedtokick").getTranslation().replace("{0}", mentioned.getName()), channel);
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                            });
                        }
                        else sendRetrievedTranslation(channel, "kick", language, "cannotkickuser");
                    }
                }
            }
            else sendRetrievedTranslation(channel, "other", language, "needkickperms");
        }
    }

    @Override
    public void setupSubcommands() {}
}
