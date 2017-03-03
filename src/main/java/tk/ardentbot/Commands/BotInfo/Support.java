package tk.ardentbot.Commands.BotInfo;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Support extends BotCommand {
    public Support(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(getTranslation("support", language, "support").getTranslation().replace("{0}", "https://ardentbot.tk/guild"), channel);
    }

    @Override
    public void setupSubcommands() {

    }
}
