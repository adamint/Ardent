package tk.ardentbot.commands.games;

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
        Random rand = new Random();
        int random = 1000 + rand.nextInt(5000);
        Profile profile = Profile.get(user);
        profile.addMoney(random);
        sendTranslatedMessage("Your daily bonus was **" + random + "**", channel, user);


    }


    @Override
    public void setupSubcommands() {
    }
}
