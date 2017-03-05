package tk.ardentbot.Commands.Fun;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import tk.ardentbot.Backend.Commands.BotCommand;
import tk.ardentbot.Backend.Translation.Language;
import tk.ardentbot.Backend.Web.Models.EightBallResponse;
import tk.ardentbot.Utils.Discord.GuildUtils;

import static tk.ardentbot.Main.Config.gson;

public class EightBall extends BotCommand {
    public EightBall(CommandSettings commandSettings) {
        super(commandSettings);
    }

    @Override
    public void noArgs(Guild guild, MessageChannel channel, User user, Message message, String[] args, Language
            language) throws Exception {
        if (args.length == 1) {
            sendRetrievedTranslation(channel, "8ball", language, "addargs");
        }
        else {
            String query = message.getRawContent().replace(GuildUtils.getPrefix(guild) + args[0] + " ", "");
            try {
                String json = Unirest.get("https://8ball.delegator.com/magic/JSON/" + query).asString().getBody();
                EightBallResponse eightBallResponse = gson.fromJson(json, EightBallResponse.class);
                sendTranslatedMessage(eightBallResponse.getMagic().getAnswer(), channel);
            }
            catch (UnirestException e) {
                sendRetrievedTranslation(channel, "other", language, "somethingwentwrong");
                e.printStackTrace();
            }

        }
    }

    @Override
    public void setupSubcommands() {
    }
}
