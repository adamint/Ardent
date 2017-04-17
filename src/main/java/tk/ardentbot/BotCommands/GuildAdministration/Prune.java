package tk.ardentbot.botCommands.guildAdministration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.misc.loggingUtils.BotException;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.utils.discord.GuildUtils;

public class Prune extends Command {
    public Prune(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage(getTranslation("prune", language, "help").getTranslation().replace("{0}",
                    GuildUtils.getPrefix(guild) + args[0]), channel, user);
        }
        else {
            if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                try {
                    int day = Integer.parseInt(args[1]);
                    if (day <= 0) {
                        sendRetrievedTranslation(channel, "prune", language, "atleastaday", user);
                        return;
                    }
                    guild.getController().prune(day).queue(integer -> {
                        try {
                            sendTranslatedMessage(getTranslation("prune", language, "success").getTranslation()
                                    .replace("{0}", String.valueOf(integer)), channel, user);
                        }
                        catch (Exception e) {
                            new BotException(e);
                        }
                    });
                }
                catch (NumberFormatException ex) {
                    sendRetrievedTranslation(channel, "prune", language, "notanumber", user);
                }
                catch (PermissionException ex) {
                    sendRetrievedTranslation(channel, "prune", language, "failure", user);
                }
            }
            else {
                sendRetrievedTranslation(channel, "other", language, "needmanageserver", user);
            }
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
