package Commands.BotInfo;

import Backend.Commands.BotCommand;
import Backend.Translation.Language;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Patreon extends BotCommand {
    public Patreon(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(getTranslation("patreon", language, "link").getTranslation(), channel);
    }

    @Override
    public void setupSubcommands() {}
}
