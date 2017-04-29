package tk.ardentbot.commands.fun;


import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;

import java.util.Random;

public class RandomNum extends Command {
    public RandomNum(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        boolean failed = false;
        if (args.length > 1) {
            try {
                int bound = Integer.parseInt(args[1]);
                if (bound <= 0) failed = true;
                else {
                    int generated = new Random().nextInt(bound) + 1;
                    sendTranslatedMessage("Generated: **{0}**".replace("{0}", String.valueOf
                            (generated)), channel, user);
                }
            } catch (NumberFormatException ex) {
                failed = true;
            }
        } else failed = true;
        if (failed) sendTranslatedMessage("The argument you provided was not a number!", channel, user);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
