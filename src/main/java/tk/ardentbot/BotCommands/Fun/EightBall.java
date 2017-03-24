package tk.ardentbot.BotCommands.Fun;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Core.CommandExecution.Command;
import tk.ardentbot.Core.Translation.Language;
import tk.ardentbot.Core.WebServer.Models.EightBallResponse;
import tk.ardentbot.Utils.Discord.GuildUtils;

public class EightBall extends Command {
    public EightBall(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "8ball", language, "addargs", user);
        }
        else {
            String query = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
            try {
                String json = Unirest.get("https://8ball.delegator.com/magic/JSON/" + query).asString().getBody();
                EightBallResponse eightBallResponse = GuildUtils.getShard(guild).gson.fromJson(json,
                        EightBallResponse.class);
                sendTranslatedMessage(eightBallResponse.getMagic().getAnswer(), channel, user);
            }
            catch (UnirestException e) {
                sendRetrievedTranslation(channel, "other", language, "somethingwentwrong", user);
                e.printStackTrace();
            }

        }
    }

    @Override
    public void setupSubcommands() {
    }
}
