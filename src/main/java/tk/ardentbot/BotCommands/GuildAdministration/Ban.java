package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;

import java.util.List;

public class Ban extends Command {
    public Ban(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("ban", language, "help").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else {
            Member userMember = guild.getMember(user);
            if (userMember.hasPermission(Permission.BAN_MEMBERS)) {
                List<User> mentionedUsers = message.getMentionedUsers();
                if (mentionedUsers.size() == 0) {
                    sendRetrievedTranslation(channel, "other", language, "mentionuserorusers");
                }
                else {
                    for (User mentioned : mentionedUsers) {
                        if (!guild.getMember(mentioned).hasPermission(userMember.getPermissions((Channel) channel))) {
                            guild.getController().ban(mentioned, 1).queue(aVoid -> {
                                try {
                                    sendTranslatedMessage(getTranslation("ban", language, "successfullybanned").getTranslation().replace("{0}", mentioned.getName()), channel);
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                            }, throwable -> {
                                try {
                                    sendTranslatedMessage(getTranslation("ban", language, "failedtoban").getTranslation().replace("{0}", mentioned.getName()), channel);
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                            });
                        }
                        else sendRetrievedTranslation(channel, "ban", language, "cannotbanuser");
                    }
                }
            }
            else sendRetrievedTranslation(channel, "other", language, "needbanperms");
        }
    }

    @Override
    public void setupSubcommands() {}
}
