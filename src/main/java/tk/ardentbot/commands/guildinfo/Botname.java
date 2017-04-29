package tk.ardentbot.commands.guildinfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.misc.logging.BotException;
import tk.ardentbot.utils.discord.GuildUtils;

public class Botname extends Command {
    public Botname(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args) throws Exception {
        if (args.length == 1) {
            sendTranslatedMessage("Enter what you want me to nickname myself", channel, user);
        }
        else {
            String name = message.getContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
            if (name.equalsIgnoreCase("reset")) {
                name = "";
            }
            try {
                guild.getController().setNickname(guild.getSelfMember(), name).queue(aVoid -> {
                    try {
                        sendTranslatedMessage("Changed my nickname!", channel, user);
                    }
                    catch (Exception e) {
                        new BotException(e);
                    }
                });
            }
            catch (PermissionException ex) {
                sendTranslatedMessage("I couldn't change my nickname, make sure I have permission to do that", channel, user);
            }
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
