package Commands.BotInfo;

import Backend.Commands.BotCommand;
import Backend.Translation.Language;
import Events.Join;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Joinmessage extends BotCommand {
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
