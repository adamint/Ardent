package tk.ardentbot.BotCommands.BotInfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Misc.LoggingUtils.BotException;
import tk.ardentbot.Core.Translation.Language;

public class Ping extends Command {
    public Ping(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        long currentTime = System.currentTimeMillis();
        try {
            channel.sendMessage("*" + getTranslation("ping", language, "calculating").getTranslation() + "*").queue(message1 -> {
                long responseTime = System.currentTimeMillis() - currentTime;
                try {
                    message1.editMessage(getTranslation("ping", language, "ping").getTranslation().replace("{0}", Long.toString
                            (responseTime))).queue();

                }
                catch (Exception e) {
                    new BotException(e);
                }
            });
        }
        catch (PermissionException e) {
            sendFailed(user, false);
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
