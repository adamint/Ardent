package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.utils.discord.GuildUtils;

public class Prune extends Command {
    public Prune(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage("Prune\n" +
                    "Prune users who have been inactive a certain amount of days\n" +
                    "\n" +
                    "Syntax: {0}prune [amount of days]\n" +
                    "Example usage: {0}prune 4".replace("{0}",
                            GuildUtils.getPrefix(guild) + args[0]), channel, user);
        } else {
            if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                try {
                    int day = Integer.parseInt(args[1]);
                    if (day <= 0) {
                        sendTranslatedMessage("Your number has to be at least 1!", channel, user);
                        return;
                    }
                    guild.getController().prune(day).queue(integer -> {
                        try {
                            sendTranslatedMessage("Successfully pruned{0} users."
                                    .replace("{0}", String.valueOf(integer)), channel, user);
                        } catch (Exception e) {
                            new BotException(e);
                        }
                    });
                } catch (NumberFormatException ex) {
                    sendTranslatedMessage("You did not supply a whole number.", channel, user);
                } catch (PermissionException ex) {
                    sendTranslatedMessage("I don't have permissions to kick users", channel, user);
                }
            } else sendTranslatedMessage("You don't have permission to kick users.", channel, user);
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
