package tk.ardentbot.BotCommands.RPG;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;
import tk.ardentbot.Utils.RPGUtils.RPGUtils;

import java.util.List;

public class Pay extends Command {
    public Pay(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        String[] raw = message.getRawContent().split(" ");
        if (raw.length == 3) {
            List<User> mentionedUsers = message.getMentionedUsers();
            if (mentionedUsers.size() > 0) {
                User mentioned = mentionedUsers.get(0);
                if (mentioned.getId().equalsIgnoreCase(user.getId()) && mentioned.getId().equalsIgnoreCase(guild.getSelfMember().getUser
                        ().getId())) {
                    sendRetrievedTranslation(channel, "other", language, "nicetry", user);
                    return;
                }
                try {
                    double amount = Double.parseDouble(raw[2]);
                    Profile profile = Profile.get(user);
                    if (amount <= 0 || profile.getMoney() < amount) {
                        sendRetrievedTranslation(channel, "pay", language, "nope", user);
                        return;
                    }
                    Profile.get(mentioned).addMoney(amount);
                    profile.removeMoney(amount);
                    sendEditedTranslation("pay", language, "paid", user, channel, user.getName(), mentioned.getName(), RPGUtils
                            .formatMoney(amount));
                }
                catch (NumberFormatException ex) {
                    sendRetrievedTranslation(channel, "prune", language, "notanumber", user);
                }
            }
            else sendRetrievedTranslation(channel, "other", language, "mentionuser", user);
        }
        else {
            sendRetrievedTranslation(channel, "pay", language, "syntax", user);
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
