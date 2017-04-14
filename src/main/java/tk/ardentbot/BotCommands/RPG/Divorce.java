package tk.ardentbot.BotCommands.RPG;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Rethink.Models.Marriage;
import tk.ardentbot.Utils.Discord.UserUtils;
import tk.ardentbot.Utils.RPGUtils.Profiles.Profile;

import java.security.SecureRandom;

import static tk.ardentbot.Rethink.Database.connection;
import static tk.ardentbot.Rethink.Database.r;

public class Divorce extends Command {
    public Divorce(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        Marriage marriage = Marry.getMarriage(user);
        if (marriage == null) sendRetrievedTranslation(channel, "divorce", language, "notmarried", user);
        else {
            sendRetrievedTranslation(channel, "divorce", language, "areyousure", user);
            interactiveOperation(language, channel, message, responseMessage -> {
                if (responseMessage.getContent().equalsIgnoreCase("yes")) {
                    r.db("data").table("marriages").filter(row -> row.g("user_one").eq(user.getId()).or(row.g
                            ("user_two").eq(user.getId()))).delete().run(connection);
                    sendRetrievedTranslation(channel, "divorce", language, "divorced", user);
                    boolean takeAllMoney = new SecureRandom().nextBoolean();
                    if (takeAllMoney) {
                        Profile userProfile = Profile.get(user);
                        Profile divorceeProfile = Profile.get(UserUtils.getUserById(marriage.getUser_one().equals(user.getId()) ?
                                marriage.getUser_two() : marriage.getUser_one()));
                        divorceeProfile.addMoney(userProfile.getMoney());
                        userProfile.removeMoney(userProfile.getMoney());
                        sendRetrievedTranslation(channel, "divorce", language, "divorcesettlement", user);
                    }
                }
                else sendRetrievedTranslation(channel, "divorce", language, "okcancelling", user);
            });
        }
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
