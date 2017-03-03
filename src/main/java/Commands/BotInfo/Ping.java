package Commands.BotInfo;

import Backend.Commands.BotCommand;
import Backend.Translation.Language;
import Bot.BotException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

public class Ping extends BotCommand {
    public Ping(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        long currentTime = System.currentTimeMillis();
        channel.sendMessage("*" + getTranslation("ping", language, "calculating").getTranslation() + "*").queue(message1 -> {
            long responseTime = System.currentTimeMillis() - currentTime;
            try {
                message1.editMessage(getTranslation("ping", language, "ping").getTranslation().replace("{0}", Long.toString(responseTime))).queue();
            }
            catch (Exception e) {
                new BotException(e);
            }
        });
    }

    @Override
    public void setupSubcommands() {

    }
}
