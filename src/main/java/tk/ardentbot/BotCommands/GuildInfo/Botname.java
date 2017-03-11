package tk.ardentbot.BotCommands.GuildInfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Cmd;
import tk.ardentbot.Core.Exceptions.BotException;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;

public class Botname extends Cmd {
    public Botname(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "botname", language, "entername");
        }
        else {
            String name = message.getContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
            if (name.equalsIgnoreCase("reset")) {
                name = "";
            }
            guild.getController().setNickname(guild.getSelfMember(), name).queue(aVoid -> {
                try {
                    sendRetrievedTranslation(channel, "botname", language, "changed");
                }
                catch (Exception e) {
                    new BotException(e);
                }
            }, throwable -> {
                try {
                    sendRetrievedTranslation(channel, "botname", language, "failed");
                }
                catch (Exception e) {
                    new BotException(e);
                }
            });
        }
    }

    @Override
    public void setupSubcommands() {}
}
