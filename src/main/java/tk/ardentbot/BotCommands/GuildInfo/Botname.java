package tk.ardentbot.botCommands.guildInfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.PermissionException;
import tk.ardentbot.core.commandExecutor.Command;
import tk.ardentbot.core.misc.loggingUtils.BotException;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.utils.discord.GuildUtils;

public class Botname extends Command {
    public Botname(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "botname", language, "entername", user);
        }
        else {
            String name = message.getContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
            if (name.equalsIgnoreCase("reset")) {
                name = "";
            }
            try {
                guild.getController().setNickname(guild.getSelfMember(), name).queue(aVoid -> {
                    try {
                        sendRetrievedTranslation(channel, "botname", language, "changed", user);
                    }
                    catch (Exception e) {
                        new BotException(e);
                    }
                });
            }
            catch (PermissionException ex) {
                sendRetrievedTranslation(channel, "botname", language, "failed", user);
            }
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
