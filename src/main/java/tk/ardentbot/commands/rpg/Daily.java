package tk.ardentbot.commands.rpg;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.utils.rpg.profiles.Profile;

import java.util.Random;

public class Daily extends Command {
    public Daily(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        Profile profile = Profile.get(user);
        if (profile.canCollect()) {
            Random rand = new Random();
            int random = rand.nextInt(1000);
            profile.addMoney(random);
            sendTranslatedMessage("Your daily bonus was **$" + random + "**, you can use this again in 24 hours!", channel, user);
            profile.setCollected();
        }
        else {
            sendTranslatedMessage(profile.getCollectionTime(), channel, user);
        }
    }


    @Override
    public void setupSubcommands() {
    }
}
