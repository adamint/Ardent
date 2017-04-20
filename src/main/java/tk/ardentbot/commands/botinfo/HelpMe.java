package tk.ardentbot.commands.botinfo;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.executor.Command;
import tk.ardentbot.Core.translate.Language;

public class HelpMe extends Command {
    public HelpMe(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        sendTranslatedMessage("Which category are you having trouble with? **music**, **language**, **notsending** - type one of those",
                channel, user);
        interactiveOperation(language, channel, message, categoryMessage -> {
            String category = categoryMessage.getContent();
            if (category.equalsIgnoreCase("music")) {
                sendTranslatedMessage("First, reconnect to the channel! It could just be discord messing up. If not, make sure Ardent has" +
                        " permission to connect to that voice channel and speak. If that doesn't" +
                        " work, type /music leave, which will reset the player. If it continues to have issues, go to our guild here: " +
                        "https://discordapp.com/invite/rfGSxNA and explain your issue **in detail**", channel, user);
            }
            else if (category.equalsIgnoreCase("language")) {
                sendTranslatedMessage("If you ever want to reset your language, type @Ardent english - if you forget your language, you " +
                        "can do " +
                        "@Ardent <nameoflanguagehere>. If you cannot set your language, something's really wrong. Message Adam#9261 with " +
                        "your issue", channel, user);
            }
            else if (category.equalsIgnoreCase("notsending")) {
                sendTranslatedMessage("I need permission to send **embeds** and **messages**! Without this, I can't respond to you guys " +
                        ":frowning: - If this continues to happen, check your command usage with /help", channel, user);
            }
            else sendTranslatedMessage("Invalid category... cancelling", channel, user);
        });
    }

    @Override
    public void setupSubcommands() throws Exception {
    }
}
