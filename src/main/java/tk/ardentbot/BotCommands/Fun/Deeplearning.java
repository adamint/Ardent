package tk.ardentbot.botCommands.fun;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.translation.Language;

public class Deeplearning extends Command {
    public Deeplearning(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {

    }

    @Override
    public void setupSubcommands() throws Exception {

    }
}
