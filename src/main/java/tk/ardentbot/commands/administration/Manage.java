package tk.ardentbot.commands.administration;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.executor.Command;
import tk.ardentbot.Core.translate.Language;

public class Manage extends Command {
    public Manage(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        /*/sendTranslatedMessage(getTranslation("manage", language, "manage").getTranslation().replace("{0}",
                "https://ardentbot.tk/portal?id=" + guild.getId()), channel, user);*/
        channel.sendMessage("The portal is temporarily disabled due to our recent database change").queue();
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
