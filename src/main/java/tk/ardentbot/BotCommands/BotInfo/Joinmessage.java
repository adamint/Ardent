package tk.ardentbot.BotCommands.BotInfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Cmd;
import tk.ardentbot.Core.Events.Join;
import tk.ardentbot.Core.Translation.Language;

public class Joinmessage extends Cmd {
    public Joinmessage(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(Join.welcomeText, channel);
    }

    @Override
    public void setupSubcommands() {
    }
}
