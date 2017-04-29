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

import java.util.List;

public class Setnickname extends Command {
    public Setnickname(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage("Set Nicknames\n" +
                    "Use {0}setnickname @User then type the nickname you want to set\n" +
                    "Example: {0}setnickname @TestUser this is your new nickname!\n" +
                    "\n" +
                    "Only people with `Manage Server` can use this command!".replace("{0}",
                            GuildUtils.getPrefix(guild) + args[0]), channel, user);
        }
        else {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() == 1) {
                if (guild.getMember(user).hasPermission(Permission.MANAGE_SERVER)) {
                    User mentioned = mentionedUsers.get(0);
                    String newNickname = tk.ardentbot.utils.StringUtils.concantenate(2, message.getRawContent().split
                            (" "));
                    while (newNickname.startsWith(" ")) newNickname = newNickname.substring(1);
                    if (newNickname.length() > 32 && newNickname.length() < 2) {
                        sendTranslatedMessage("The nickname must be between 2 and 32 characters!", channel, user);
                    }
                    else {
                        if (newNickname.equalsIgnoreCase("reset")) newNickname = "";
                        String finalNewNickname = newNickname;
                        try {
                            guild.getController().setNickname(guild.getMember(mentioned), newNickname).queue(aVoid -> {
                                try {
                                    sendTranslatedMessage("Great success! Changed **{0}**'s nickname to {1}".replace("{0}", mentioned
                                            .getName()).replace("{1}", finalNewNickname), channel, user);
                                }
                                catch (Exception e) {
                                    new BotException(e);
                                }
                            });
                        }
                        catch (PermissionException e) {
                            sendTranslatedMessage("Please make sure that I have permission to set members' nicknames", channel, user);
                        }
                    }
                }
                else sendTranslatedMessage("You need the Manage Server permission to use this command", channel, user);
            }
            else sendTranslatedMessage("You need to mention someone!", channel, user);
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
