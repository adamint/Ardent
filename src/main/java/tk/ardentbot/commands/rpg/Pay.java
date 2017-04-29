package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.rpg.RPGUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.List;

public class Pay extends Command {
    public Pay(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        String[] raw = message.getRawContent().split(" ");
        if (raw.length == 3) {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() > 0) {
                User mentioned = mentionedUsers.get(0);
                if (mentioned.getId().equalsIgnoreCase(user.getId()) || mentioned.getId().equalsIgnoreCase(guild.getSelfMember().getUser
                        ().getId()))
                {
                    sendTranslatedMessage("You can't pay yourself or me, lol", channel, user);
                    return;
                }
                try {
                    double amount = Double.parseDouble(raw[2]);
                    Profile profile = Profile.get(user);
                    if (amount <= 0 || profile.getMoney() < amount) {
                        sendTranslatedMessage("You don't have enough money to do this!", channel, user);
                        return;
                    }
                    Profile.get(mentioned).addMoney(amount);
                    profile.removeMoney(amount);
                    sendEditedTranslation("**{0}** paid **{1}** {2}", user, channel, user.getName(), mentioned.getName(), RPGUtils
                            .formatMoney(amount));
                }
                catch (NumberFormatException ex) {
                    sendTranslatedMessage("That's not a number!", channel, user);
                }
            }
            else sendTranslatedMessage("You need to mention a user", channel, user);
        }
        else {
            sendTranslatedMessage("Use /pay @User [amount]", channel, user);

        }
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
