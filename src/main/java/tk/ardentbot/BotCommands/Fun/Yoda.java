package tk.ardentbot.BotCommands.Fun;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Utils.Discord.GuildUtils;

public class Yoda extends Command {
    public Yoda(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "yoda", language, "help");
        }
        else {
            try {
                channel.sendTyping().queue();
                HttpResponse<String> response = Unirest.get("https://yoda.p.mashape.com/yoda?sentence=" + message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "").replace(" ", "+"))
                        .header("X-Mashape-Key", "ReLLUaXvjImsh0IJ2pe3NoLknUMip1RW4fkjsn3ajtoFnaOCal")
                        .header("Accept", "text/plain")
                        .asString();
                sendTranslatedMessage(response.getBody(), channel);
            }
            catch (UnirestException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setupSubcommands() {
    }
}
