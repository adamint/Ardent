package Commands.Fun;

import Backend.Commands.BotCommand;
import Backend.Translation.Language;
import Utils.GuildUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Echo extends BotCommand {
    public Echo(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        String echoText = message.getContent().replace("@here", "@ here").replace("@everyone", "@ everyone").replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
        channel.sendMessage(echoText).queue();
    }

    @Override
    public void setupSubcommands() {
    }
}
