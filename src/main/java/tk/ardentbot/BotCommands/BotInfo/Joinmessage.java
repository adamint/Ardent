package tk.ardentbot.botCommands.botInfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.events.Join;
import tk.ardentbot.core.translation.Language;

public class Joinmessage extends Command {
    public Joinmessage(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(Join.welcomeText, channel, user);
    }

    @Override
    public void setupSubcommands() {
    }
}
