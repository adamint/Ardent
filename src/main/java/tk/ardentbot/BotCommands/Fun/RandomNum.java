package tk.ardentbot.BotCommands.Fun;


import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;

import java.util.Random;

public class RandomNum extends Command {
    public RandomNum(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        boolean failed = false;
        if (args.length > 1) {
            try {
                int bound = Integer.parseInt(args[1]);
                if (bound <= 0) failed = true;
                else {
                    int generated = new Random().nextInt(bound) + 1;
                    sendTranslatedMessage(getTranslation("random", language, "returned").getTranslation().replace("{0}", String.valueOf
                            (generated)), channel, user);
                }
            }
            catch (NumberFormatException ex) {
                failed = true;
            }
        }
        else failed = true;
        if (failed) sendRetrievedTranslation(channel, "prune", language, "notanumber", user);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
