package tk.ardentbot.Commands.BotInfo;

import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Invite extends BotCommand {
    public Invite(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage(getTranslation("invite", language, "invite").getTranslation().replace("{0}", "https://ardentbot.tk/invite"), channel);
    }

    @Override
    public void setupSubcommands() {
    }
}
