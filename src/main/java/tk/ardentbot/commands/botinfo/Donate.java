package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;

public class Donate extends Command {
    public Donate(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        sendTranslatedMessage("Want to support Ardent? Pledge at https://patreon.com/ardent and receive perks!\n You can also donate directly at https://www.paypal.me/ardentbot", channel, user);
    }

    @Override
    public void setupSubcommands() {
    }
}
