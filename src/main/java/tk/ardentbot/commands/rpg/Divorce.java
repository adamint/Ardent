package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.rethink.models.Marriage;
import tk.ardentbot.utils.discord.UserUtils;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.security.SecureRandom;

import static tk.ardentbot.rethink.Database.connection;
import static tk.ardentbot.rethink.Database.r;

public class Divorce extends Command {
    public Divorce(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        Marriage marriage = Marry.getMarriage(user);
        if (marriage == null) sendTranslatedMessage("You're not married!", channel, user);
        else {
            sendTranslatedMessage("Are you sure you want to divorce this person? There's a 50% chance that half of your assets will be " +
                    "transferred to them. Respond **yes** if you want to go through with the divorce, or **no** if not.", channel, user);
            interactiveOperation(channel, message, responseMessage -> {
                if (responseMessage.getContent().equalsIgnoreCase("yes")) {
                    r.db("data").table("marriages").filter(row -> row.g("user_one").eq(user.getId()).or(row.g
                            ("user_two").eq(user.getId()))).delete().run(connection);
                    sendTranslatedMessage("You're now single", channel, user);
                    boolean takeAllMoney = !new SecureRandom().nextBoolean();
                    if (takeAllMoney) {
                        Profile userProfile = Profile.get(user);
                        Profile divorceeProfile = Profile.get(UserUtils.getUserById(marriage.getUser_one().equals(user.getId()) ?
                                marriage.getUser_two() : marriage.getUser_one()));
                        divorceeProfile.addMoney(userProfile.getMoney() / 2);
                        userProfile.removeMoney(userProfile.getMoney() / 2);
                        sendTranslatedMessage("Unlucky! Half of your assets were transferred to your ex.", channel, user);
                    }
                }
                else sendTranslatedMessage("Ok, cancelling the divorce, but you should probably go to couples' therapy.", channel, user);
            });
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
