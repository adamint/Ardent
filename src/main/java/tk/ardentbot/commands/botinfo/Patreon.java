package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.executor.Command;
import tk.ardentbot.Core.translate.Language;

public class Patreon extends Command {
    public Patreon(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendTranslatedMessage(getTranslation("patreon", language, "link").getTranslation() + "\n" + getTranslation
                ("patreon", language, "atleast2").getTranslation(), channel, user);
    }

    @Override
    public void setupSubcommands() {
    }
}
