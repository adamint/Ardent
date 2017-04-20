package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.events.Join;
import tk.ardentbot.Core.executor.Command;
import tk.ardentbot.Core.translate.Language;

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
