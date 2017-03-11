package tk.ardentbot.BotCommands.GuildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.Core.CommandExecution.Cmd;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;

public class Prune extends Cmd {
    public Prune(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("prune", language, "help").getTranslation().replace("{0}", GuildUtils.getPrefix(guild) + args[0]), channel);
        }
        else {
            if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                try {
                    int day = Integer.parseInt(args[1]);
                    if (day <= 0) {
                        sendRetrievedTranslation(channel, "prune", language, "atleastaday");
                        return;
                    }
                    guild.getController().prune(day).queue(integer -> {
                        try {
                            sendTranslatedMessage(getTranslation("prune", language, "success").getTranslation().replace("{0}", String.valueOf(integer)), channel);
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    });
                }
                catch (NumberFormatException ex) {
                    sendRetrievedTranslation(channel, "prune", language, "notanumber");
                }
                catch (PermissionException ex) {
                    sendRetrievedTranslation(channel, "prune", language, "failure");
                }
            }
            else {
                sendRetrievedTranslation(channel, "other", language, "needmanageserver");
            }
        }
    }

    @Override
    public void setupSubcommands() {}
}
