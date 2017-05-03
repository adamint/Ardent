package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.utils.discord.UserUtils;

import java.util.List;

public class SoftBan extends Command {
    public SoftBan(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        Member userMember = guild.getMember(user);
        if (userMember.hasPermission(Permission.BAN_MEMBERS)) {
            List<User> mentioned = message.getMentionedUsers();
            try {
                User u = mentioned.get(0);
                String userString = u.toString();
                String uToSend = UserUtils.getNameWithDiscriminator(userString);

                try {
                    guild.getController().ban(u, 7).queue();
                    guild.getController().unban(u).queue();
                    sendTranslatedMessage("Soft-banned **" + uToSend + "**", channel, user);

                } catch (PermissionException ex) {
                    sendTranslatedMessage("I don't have permissions to ban!", channel, user);
                }
            } catch (Exception e) {
                new BotException(e);

            }
        } else {
            sendTranslatedMessage("You don't have the: ```BAN MEMBERS``` permission", channel, user);
        }
    }

    @Override
    public void setupSubcommands() {

    }
}
