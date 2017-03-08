package tk.ardentbot.Commands.GuildAdministration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;

public class Manage extends BotCommand {
    public Manage(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        sendTranslatedMessage(getTranslation("manage", language, "manage").getTranslation().replace("{0}",
                "https://ardentbot.tk/portal?id=" + guild.getId()), channel);
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
