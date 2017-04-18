package tk.ardentbot.commands.fun;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.core.executor.Command;
import tk.ardentbot.core.translation.Language;
import tk.ardentbot.utils.discord.GuildUtils;

import java.net.URLEncoder;

public class Yoda extends Command {
    public Yoda(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "yoda", language, "help", user);
        }
        else {
            try {
                channel.sendTyping().queue();
                HttpResponse<String> response = Unirest.get("https://yoda.p.mashape.com/yoda?sentence=" + URLEncoder.encode(message
                        .getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "").replace(" ", "+")))
                        .header("X-Mashape-Key", "ReLLUaXvjImsh0IJ2pe3NoLknUMip1RW4fkjsn3ajtoFnaOCal")
                        .header("Accept", "text/plain")
                        .asString();
                String body = response.getBody();
                if (body.contains("<html>")) {
                    sendRetrievedTranslation(channel, "yoda", language, "unavailable", user);
                }
                else {
                    sendTranslatedMessage(response.getBody(), channel, user);
                }
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
