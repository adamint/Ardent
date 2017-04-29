package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.core.executor.Ratelimitable;
import tk.ardentbot.core.misc.logging.BotException;

public class Ping extends Ratelimitable {
    public Ping(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        long currentTime = System.currentTimeMillis();
        try {
            channel.sendMessage("*Calculating ping now*").queue(message1 -> {
                long responseTime = System.currentTimeMillis() - currentTime;
                try {
                    message1.editMessage("Current ping: {0} ms".replace("{0}", Long.toString
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
